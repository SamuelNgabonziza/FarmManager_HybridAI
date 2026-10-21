package com.samuel.farmos.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class FarmRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    private fun farmsCollection(uid: String) =
        db.collection("users").document(uid).collection("farms")

    private fun animalsCollection(uid: String, farmId: String) =
        farmsCollection(uid).document(farmId).collection("animals")

    private fun tasksCollection(uid: String, farmId: String) =
        farmsCollection(uid).document(farmId).collection("tasks")

    private fun productionCollection(uid: String, farmId: String) =
        farmsCollection(uid).document(farmId).collection("production")

    private fun breedingCollection(uid: String, farmId: String) =
        farmsCollection(uid).document(farmId).collection("breeding")

    private fun feedingCollection(uid: String, farmId: String) =
        farmsCollection(uid).document(farmId).collection("feeding")

    private fun financeCollection(uid: String, farmId: String) =
        farmsCollection(uid).document(farmId).collection("finances")

    private fun healthCaseCollection(uid: String, farmId: String) =
        farmsCollection(uid).document(farmId).collection("healthCases")

    // Farms
    suspend fun getFarms(uid: String): List<Farm> {
        val snapshot = farmsCollection(uid).get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Farm::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun createFarm(uid: String, farmName: String): String {
        val docRef = farmsCollection(uid).document()
        val farm = Farm(id = docRef.id, farmName = farmName)
        docRef.set(farm).await()
        return docRef.id
    }

    suspend fun updateFarmName(uid: String, farmId: String, newName: String) {
        farmsCollection(uid).document(farmId).update("farmName", newName).await()
    }

    suspend fun setAdminPin(uid: String, farmId: String, pin: String) {
        farmsCollection(uid).document(farmId).update("adminPin", pin).await()
    }

    suspend fun getFarm(uid: String, farmId: String): Farm? {
        val doc = farmsCollection(uid).document(farmId).get().await()
        return doc.toObject(Farm::class.java)?.copy(id = doc.id)
    }

    suspend fun deleteFarm(uid: String, farmId: String) {
        farmsCollection(uid).document(farmId).delete().await()
    }

    // Animals
    fun getAnimals(uid: String, farmId: String): Flow<List<Animal>> = callbackFlow {
        val listener = animalsCollection(uid, farmId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val animals = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Animal::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(animals)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addAnimal(uid: String, farmId: String, animal: Animal): String {
        val docRef = animalsCollection(uid, farmId).document()
        val newAnimal = animal.copy(id = docRef.id)
        docRef.set(newAnimal).await()
        return docRef.id
    }

    suspend fun updateAnimal(uid: String, farmId: String, animal: Animal) {
        animalsCollection(uid, farmId).document(animal.id).set(animal).await()
    }

    suspend fun deleteAnimal(uid: String, farmId: String, animalId: String) {
        animalsCollection(uid, farmId).document(animalId).delete().await()
    }

    // Tasks
    fun getTasks(uid: String, farmId: String): Flow<List<Task>> = callbackFlow {
        val listener = tasksCollection(uid, farmId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val tasks = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Task::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(tasks)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addTask(uid: String, farmId: String, task: Task): String {
        val docRef = tasksCollection(uid, farmId).document()
        val newTask = task.copy(id = docRef.id)
        docRef.set(newTask).await()
        return docRef.id
    }

    suspend fun updateTask(uid: String, farmId: String, task: Task) {
        tasksCollection(uid, farmId).document(task.id).set(task).await()
    }

    suspend fun deleteTask(uid: String, farmId: String, taskId: String) {
        tasksCollection(uid, farmId).document(taskId).delete().await()
    }

    suspend fun completeTask(uid: String, farmId: String, task: Task) {
        val nextDue = calculateNextDueDate(task.dueDateMillis, task.recurrence)
        if (nextDue != null && task.recurrence != Recurrence.NONE) {
            val updated = task.copy(dueDateMillis = nextDue, completed = false)
            updateTask(uid, farmId, updated)
        } else {
            val updated = task.copy(completed = true)
            updateTask(uid, farmId, updated)
        }
    }

    private fun calculateNextDueDate(currentDueMillis: Long, recurrence: Recurrence): Long? {
        if (recurrence == Recurrence.NONE) return null
        val cal = Calendar.getInstance()
        cal.timeInMillis = if (currentDueMillis > 0) currentDueMillis else System.currentTimeMillis()
        when (recurrence) {
            Recurrence.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            Recurrence.WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            Recurrence.MONTHLY -> cal.add(Calendar.MONTH, 1)
            Recurrence.NONE -> return null
        }
        return cal.timeInMillis
    }

    // Production Records
    fun getProductionRecords(uid: String, farmId: String): Flow<List<ProductionRecord>> = callbackFlow {
        val listener = productionCollection(uid, farmId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ProductionRecord::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addProductionRecord(uid: String, farmId: String, record: ProductionRecord): String {
        val docRef = productionCollection(uid, farmId).document()
        val newRecord = record.copy(id = docRef.id)
        docRef.set(newRecord).await()
        return docRef.id
    }

    suspend fun updateProductionRecord(uid: String, farmId: String, record: ProductionRecord) {
        productionCollection(uid, farmId).document(record.id).set(record).await()
    }

    suspend fun deleteProductionRecord(uid: String, farmId: String, recordId: String) {
        productionCollection(uid, farmId).document(recordId).delete().await()
    }

    // Breeding Records
    fun getBreedingRecords(uid: String, farmId: String): Flow<List<BreedingRecord>> = callbackFlow {
        val listener = breedingCollection(uid, farmId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(BreedingRecord::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addBreedingRecord(uid: String, farmId: String, record: BreedingRecord): String {
        val docRef = breedingCollection(uid, farmId).document()
        val newRecord = record.copy(id = docRef.id)
        docRef.set(newRecord).await()
        return docRef.id
    }

    suspend fun updateBreedingRecord(uid: String, farmId: String, record: BreedingRecord) {
        breedingCollection(uid, farmId).document(record.id).set(record).await()
    }

    suspend fun deleteBreedingRecord(uid: String, farmId: String, recordId: String) {
        breedingCollection(uid, farmId).document(recordId).delete().await()
    }

    // Feeding Records
    fun getFeedingRecords(uid: String, farmId: String): Flow<List<FeedingRecord>> = callbackFlow {
        val listener = feedingCollection(uid, farmId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(FeedingRecord::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addFeedingRecord(uid: String, farmId: String, record: FeedingRecord): String {
        val docRef = feedingCollection(uid, farmId).document()
        val newRecord = record.copy(id = docRef.id)
        docRef.set(newRecord).await()
        return docRef.id
    }

    suspend fun updateFeedingRecord(uid: String, farmId: String, record: FeedingRecord) {
        feedingCollection(uid, farmId).document(record.id).set(record).await()
    }

    suspend fun deleteFeedingRecord(uid: String, farmId: String, recordId: String) {
        feedingCollection(uid, farmId).document(recordId).delete().await()
    }

    // Finance Records
    fun getFinanceRecords(uid: String, farmId: String): Flow<List<FinanceRecord>> = callbackFlow {
        val listener = financeCollection(uid, farmId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(FinanceRecord::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addFinanceRecord(uid: String, farmId: String, record: FinanceRecord): String {
        val docRef = financeCollection(uid, farmId).document()
        val newRecord = record.copy(id = docRef.id)
        docRef.set(newRecord).await()
        return docRef.id
    }

    suspend fun updateFinanceRecord(uid: String, farmId: String, record: FinanceRecord) {
        financeCollection(uid, farmId).document(record.id).set(record).await()
    }

    suspend fun deleteFinanceRecord(uid: String, farmId: String, recordId: String) {
        financeCollection(uid, farmId).document(recordId).delete().await()
    }

    // Health Cases
    fun getHealthCases(uid: String, farmId: String): Flow<List<HealthCase>> = callbackFlow {
        val listener = healthCaseCollection(uid, farmId).addSnapshotListener { snapshot, e ->
            if (e != null) {
                close(e)
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(HealthCase::class.java)?.copy(id = doc.id)
            } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    suspend fun addHealthCase(uid: String, farmId: String, healthCase: HealthCase): String {
        val docRef = healthCaseCollection(uid, farmId).document()
        val newCase = healthCase.copy(id = docRef.id)
        docRef.set(newCase).await()
        return docRef.id
    }

    suspend fun updateHealthCase(uid: String, farmId: String, healthCase: HealthCase) {
        healthCaseCollection(uid, farmId).document(healthCase.id).set(healthCase).await()
    }

    suspend fun deleteHealthCase(uid: String, farmId: String, caseId: String) {
        healthCaseCollection(uid, farmId).document(caseId).delete().await()
    }
}
