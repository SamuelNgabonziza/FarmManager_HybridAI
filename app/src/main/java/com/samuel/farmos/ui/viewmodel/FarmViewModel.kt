package com.samuel.farmos.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.BreedingRecord
import com.samuel.farmos.data.FeedingRecord
import com.samuel.farmos.data.FinanceRecord
import com.samuel.farmos.data.Farm
import com.samuel.farmos.data.FarmRepository
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.HealthCaseStatus
import com.samuel.farmos.data.ProductionRecord
import com.samuel.farmos.data.SessionManager
import com.samuel.farmos.data.Task
import com.samuel.farmos.notifications.TaskReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Owns Firebase data access: the signed-in uid, the active farm, and every
 * collection that lives under [users/{uid}/farms/{farmId}].
 *
 * Collections are exposed as [StateFlow]s so the UI can read them with
 * `collectAsStateWithLifecycle` and react to inserts, edits, deletes and auth
 * changes without any manual refresh.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FarmViewModel(
    private val repository: FarmRepository,
    private val sessionManager: SessionManager,
    private val context: Context? = null
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uid = MutableStateFlow(auth.currentUser?.uid)
    val uid: StateFlow<String?> = _uid.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /** The farms owned by the signed-in user. */
    private val _farms = MutableStateFlow<List<Farm>>(emptyList())
    val farms: StateFlow<List<Farm>> = _farms.asStateFlow()

    /** The farm the app is currently focused on. */
    val activeFarmId: StateFlow<String?> = sessionManager.activeFarmId

    /** Display name of the active farm, or null when no farm is selected yet. */
    val currentFarmName: StateFlow<String?> = combine(_farms, activeFarmId) { list, id ->
        list.firstOrNull { it.id == id }?.farmName
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), null)

    /** The active farm's admin PIN, if one has been set. */
    val adminPin: StateFlow<String?> = combine(_farms, activeFarmId) { list, id ->
        list.firstOrNull { it.id == id }?.adminPin
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), null)

    // --- Collections -------------------------------------------------------

    val animals: StateFlow<List<Animal>> =
        scoped("animals") { uid, farmId -> repository.getAnimals(uid, farmId) }

    val tasks: StateFlow<List<Task>> =
        scoped("tasks") { uid, farmId -> repository.getTasks(uid, farmId) }

    val productionRecords: StateFlow<List<ProductionRecord>> =
        scoped("production") { uid, farmId -> repository.getProductionRecords(uid, farmId) }

    val breedingRecords: StateFlow<List<BreedingRecord>> =
        scoped("breeding") { uid, farmId -> repository.getBreedingRecords(uid, farmId) }

    val feedingRecords: StateFlow<List<FeedingRecord>> =
        scoped("feeding") { uid, farmId -> repository.getFeedingRecords(uid, farmId) }

    val financeRecords: StateFlow<List<FinanceRecord>> =
        scoped("finances") { uid, farmId -> repository.getFinanceRecords(uid, farmId) }

    val healthCases: StateFlow<List<HealthCase>> =
        scoped("health cases") { uid, farmId -> repository.getHealthCases(uid, farmId) }

    /** Open tasks that are already due or due before the end of today. */
    val todayTasks: StateFlow<List<Task>> = tasks.map { all ->
        val cutoff = endOfTodayMillis()
        all.filter { !it.completed && it.dueDateMillis <= cutoff }
            .sortedBy { it.dueDateMillis }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), emptyList())

    /** Health cases that still need attention. */
    val activeHealthCases: StateFlow<List<HealthCase>> = healthCases.map { all ->
        all.filter { it.status == HealthCaseStatus.ACTIVE }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), emptyList())

    /** Expected births falling due within the next 7 days. */
    val birthsDueSoonCount: StateFlow<Int> = breedingRecords.map { all ->
        val now = System.currentTimeMillis()
        val horizon = now + SEVEN_DAYS_MS
        all.count { record ->
            val due = record.expectedDueDateMillis
            due != null && due in now..horizon
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), 0)

    // --- Auth / farms ------------------------------------------------------

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val newUid = firebaseAuth.currentUser?.uid
        _uid.value = newUid
        if (newUid == null) {
            _farms.value = emptyList()
            _isLoading.value = false
        } else {
            viewModelScope.launch { loadFarms(newUid) }
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authListener)
    }

    private suspend fun loadFarms(uid: String) {
        _isLoading.value = true
        runCatching { repository.getFarms(uid) }
            .onSuccess { list ->
                _farms.value = list
                ensureActiveFarm(list)
                _isLoading.value = false
            }
            .onFailure { error ->
                _errorMessage.value = "Could not load farms: ${error.message}"
                _isLoading.value = false
            }
    }

    /** Keep the current selection when it still exists, otherwise fall back to the first farm. */
    private fun ensureActiveFarm(list: List<Farm>) {
        val current = sessionManager.activeFarmId.value
        val chosen = when {
            list.any { it.id == current } -> current
            list.isNotEmpty() -> list.first().id
            else -> null
        }
        if (chosen != current) {
            sessionManager.setActiveFarmId(chosen)
        }
    }

    fun selectFarm(farm: Farm) {
        sessionManager.setActiveFarmId(farm.id)
    }

    suspend fun deleteFarm(farm: Farm) {
        val uid = currentUid() ?: return
        runCatching { repository.deleteFarm(uid, farm.id) }
            .onSuccess {
                runCatching { repository.getFarms(uid) }
                    .onSuccess { list ->
                        _farms.value = list
                        ensureActiveFarm(list)
                    }
                    .onFailure { _errorMessage.value = "Farm deleted, but the farm list could not be refreshed: ${it.message}" }
            }
            .onFailure { _errorMessage.value = "Could not delete farm: ${it.message}" }
    }

    fun consumeError() {
        _errorMessage.value = null
    }

    // --- Writes -------------------------------------------------------------

    private fun activeFarm(): Farm? =
        _farms.value.firstOrNull { it.id == sessionManager.activeFarmId.value }

    private fun currentUid(): String? = auth.currentUser?.uid

    suspend fun createFarm(farmName: String) {
        val uid = currentUid() ?: return
        runCatching { repository.createFarm(uid, farmName) }
            .onSuccess {
                // Re-read the farm list so the new farm shows up (and becomes active).
                runCatching { repository.getFarms(uid) }
                    .onSuccess { list ->
                        _farms.value = list
                        ensureActiveFarm(list)
                    }
            }
            .onFailure { _errorMessage.value = "Could not create farm: ${it.message}" }
    }

    suspend fun renameFarm(farmName: String) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.updateFarmName(uid, farm.id, farmName) }
            .onFailure { _errorMessage.value = "Could not rename farm: ${it.message}" }
    }

    suspend fun setAdminPin(pin: String) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.setAdminPin(uid, farm.id, pin) }
            .onFailure { _errorMessage.value = "Could not save PIN: ${it.message}" }
    }

    suspend fun saveAnimal(animal: Animal) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching {
            if (animal.id.isBlank()) {
                repository.addAnimal(uid, farm.id, animal)
            } else {
                repository.updateAnimal(uid, farm.id, animal)
            }
        }.onFailure { _errorMessage.value = "Could not save animal: ${it.message}" }
    }

    suspend fun deleteAnimal(animalId: String) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.deleteAnimal(uid, farm.id, animalId) }
            .onFailure { _errorMessage.value = "Could not delete animal: ${it.message}" }
    }

    suspend fun saveTask(task: Task) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching {
            val taskId = if (task.id.isBlank()) {
                repository.addTask(uid, farm.id, task)
            } else {
                repository.updateTask(uid, farm.id, task)
                task.id
            }
            context?.let { ctx ->
                if (task.dueDateMillis > System.currentTimeMillis() && !task.completed) {
                    TaskReminderScheduler.schedule(ctx, task.copy(id = taskId))
                } else {
                    TaskReminderScheduler.cancel(ctx, taskId)
                }
            }
        }.onFailure { _errorMessage.value = "Could not save task: ${it.message}" }
    }

    suspend fun deleteTask(taskId: String) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching {
            repository.deleteTask(uid, farm.id, taskId)
            context?.let { ctx ->
                TaskReminderScheduler.cancel(ctx, taskId)
            }
        }.onFailure { _errorMessage.value = "Could not delete task: ${it.message}" }
    }

    suspend fun toggleTaskComplete(task: Task) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching {
            repository.completeTask(uid, farm.id, task)
            context?.let { ctx ->
                if (!task.completed) {
                    // Task is now completed -> cancel reminder
                    TaskReminderScheduler.cancel(ctx, task.id)
                } else if (task.dueDateMillis > System.currentTimeMillis()) {
                    // Task was uncompleted -> reschedule reminder
                    TaskReminderScheduler.schedule(ctx, task.copy(completed = false))
                }
            }
        }.onFailure { _errorMessage.value = "Could not update task: ${it.message}" }
    }

    suspend fun saveProduction(record: ProductionRecord) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.addProductionRecord(uid, farm.id, record) }
            .onFailure { _errorMessage.value = "Could not save production record: ${it.message}" }
    }

    suspend fun deleteProduction(recordId: String) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.deleteProductionRecord(uid, farm.id, recordId) }
            .onFailure { _errorMessage.value = "Could not delete production record: ${it.message}" }
    }

    suspend fun saveBreeding(record: BreedingRecord) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.addBreedingRecord(uid, farm.id, record) }
            .onFailure { _errorMessage.value = "Could not save breeding record: ${it.message}" }
    }

    suspend fun deleteBreeding(recordId: String) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.deleteBreedingRecord(uid, farm.id, recordId) }
            .onFailure { _errorMessage.value = "Could not delete breeding record: ${it.message}" }
    }

    suspend fun saveFeeding(record: FeedingRecord) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.addFeedingRecord(uid, farm.id, record) }
            .onFailure { _errorMessage.value = "Could not save feeding record: ${it.message}" }
    }

    suspend fun deleteFeeding(recordId: String) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.deleteFeedingRecord(uid, farm.id, recordId) }
            .onFailure { _errorMessage.value = "Could not delete feeding record: ${it.message}" }
    }

    suspend fun saveFinance(record: FinanceRecord) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.addFinanceRecord(uid, farm.id, record) }
            .onFailure { _errorMessage.value = "Could not save transaction: ${it.message}" }
    }

    suspend fun deleteFinance(recordId: String) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.deleteFinanceRecord(uid, farm.id, recordId) }
            .onFailure { _errorMessage.value = "Could not delete transaction: ${it.message}" }
    }

    suspend fun saveHealthCase(healthCase: HealthCase) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.addHealthCase(uid, farm.id, healthCase) }
            .onFailure { _errorMessage.value = "Could not save health case: ${it.message}" }
    }

    suspend fun updateHealthCaseStatus(caseId: String, status: HealthCaseStatus) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        val healthCase = healthCases.value.firstOrNull { it.id == caseId } ?: return
        runCatching {
            repository.updateHealthCase(uid, farm.id, healthCase.copy(status = status))
        }.onFailure { _errorMessage.value = "Could not update health case: ${it.message}" }
    }

    suspend fun deleteHealthCase(caseId: String) {
        val uid = currentUid() ?: return
        val farm = activeFarm() ?: return
        runCatching { repository.deleteHealthCase(uid, farm.id, caseId) }
            .onFailure { _errorMessage.value = "Could not delete health case: ${it.message}" }
    }

    fun signOut() {
        sessionManager.clear()
        auth.signOut()
    }

    // --- Helpers ------------------------------------------------------------

    /**
     * Turns a repository query into a [StateFlow] that re-subscribes whenever
     * the signed-in user or the selected farm changes.
     */
    private fun <T> scoped(
        label: String,
        source: (String, String) -> Flow<List<T>>
    ): StateFlow<List<T>> {
        val scope: Flow<Scope> = combine(_uid, sessionManager.activeFarmId) { uid, farmId ->
            Scope(uid, farmId)
        }.distinctUntilChanged()

        return scope.flatMapLatest { current ->
            if (current.uid == null || current.farmId == null) {
                flowOf(emptyList<T>())
            } else {
                source(current.uid, current.farmId)
                    .catch { error ->
                        _errorMessage.value = "Could not load $label: ${error.message}"
                        emit(emptyList<T>())
                    }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS), emptyList())
    }

    private data class Scope(val uid: String?, val farmId: String?)

    companion object {
        private const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
        private const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    ?: throw IllegalArgumentException("FarmViewModel requires the Application")
                FarmViewModel(FarmRepository(), SessionManager(application), application.applicationContext)
            }
        }

        private fun endOfTodayMillis(): Long {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            return calendar.timeInMillis
        }
    }
}
