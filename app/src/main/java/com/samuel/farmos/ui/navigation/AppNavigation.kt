package com.samuel.farmos.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samuel.farmos.ui.components.FarmSnackbarProvider
import com.samuel.farmos.ui.screens.AddBreedingScreen
import com.samuel.farmos.ui.screens.AddEditAnimalScreen
import com.samuel.farmos.ui.screens.AddEditTaskScreen
import com.samuel.farmos.ui.screens.AddFarmScreen
import com.samuel.farmos.ui.screens.AddFeedingScreen
import com.samuel.farmos.ui.screens.AddFinanceScreen
import com.samuel.farmos.ui.screens.AddHealthCaseScreen
import com.samuel.farmos.ui.screens.AddProductionScreen
import com.samuel.farmos.ui.screens.AdminScreen
import com.samuel.farmos.ui.screens.AiTabScreen
import com.samuel.farmos.ui.screens.AnimalsListScreen
import com.samuel.farmos.ui.screens.AuthScreen
import com.samuel.farmos.ui.screens.BreedingScreen
import com.samuel.farmos.ui.screens.FarmSwitcherScreen
import com.samuel.farmos.ui.screens.FeedingScreen
import com.samuel.farmos.ui.screens.FinanceScreen
import com.samuel.farmos.ui.screens.HealthCaseDetailScreen
import com.samuel.farmos.ui.screens.HealthScreen
import com.samuel.farmos.ui.screens.HomeScreen
import com.samuel.farmos.ui.screens.LoginScreen
import com.samuel.farmos.ui.screens.ProductionScreen
import com.samuel.farmos.ui.screens.SettingsScreen
import com.samuel.farmos.ui.screens.SplashScreen
import com.samuel.farmos.ui.screens.TasksScreen
import com.samuel.farmos.ui.viewmodel.FarmAiViewModel
import com.samuel.farmos.ui.viewmodel.FarmViewModel
import com.samuel.farmos.data.ProductionType
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * The single navigation graph for the app.
 *
 * Every screen reads its data from [FarmViewModel]'s `StateFlow`s, so a write
 * anywhere in the app is reflected everywhere without a manual refresh.
 *
 * @param onContentReady called once the first real screen has been composed, so
 *   the platform splash screen can be dismissed.
 */
@Composable
fun AppNavigation(
    initialDestination: String? = null,
    onContentReady: () -> Unit = {}
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val farmViewModel: FarmViewModel = viewModel(factory = FarmViewModel.Factory)
    val aiViewModel: FarmAiViewModel = viewModel()

    val uid by farmViewModel.uid.collectAsStateWithLifecycle()
    val activeFarmId by farmViewModel.activeFarmId.collectAsStateWithLifecycle()
    val errorMessage by farmViewModel.errorMessage.collectAsStateWithLifecycle()

    // Navigate to notification destination if triggered while app is running
    LaunchedEffect(initialDestination) {
        if (!initialDestination.isNullOrBlank()) {
            navController.navigate(initialDestination)
        }
    }

    // The assistant must operate on the same farm the rest of the app is using.
    LaunchedEffect(uid, activeFarmId) {
        aiViewModel.setFarmContext(uid, activeFarmId)
    }

    // Failed writes and reads are reported once, in a snackbar, rather than
    // silently leaving the user staring at an unchanged list.
    LaunchedEffect(errorMessage) {
        val message = errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        farmViewModel.consumeError()
    }

    // The splash screen is the first thing composed, so the platform splash can
    // be released as soon as it is on screen.
    LaunchedEffect(Unit) { onContentReady() }

    val darkMode = isSystemInDarkTheme()
    val pageGradient = if (darkMode) {
        listOf(Color(0xFF26312A), Color(0xFF35443A), Color(0xFF4B5C4A))
    } else {
        listOf(Color(0xFFF7FBF5), Color(0xFFF0F7F0), Color(0xFFE8F1E7))
    }
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(pageGradient))) {
      FarmSnackbarProvider(hostState = snackbarHostState) {
        NavHost(navController = navController, startDestination = Routes.SPLASH) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onFinished = { signedIn ->
                        if (signedIn) {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                            if (!initialDestination.isNullOrBlank()) {
                                navController.navigate(initialDestination)
                            }
                        } else {
                            navController.navigate(Routes.AUTH) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Routes.AUTH) {
                AuthScreen(
                    onSkip = { navController.navigate(Routes.HOME) },
                    onAuthSuccess = { _, initialFarmName ->
                        scope.launch {
                            if (!initialFarmName.isNullOrBlank()) {
                                farmViewModel.createFarm(initialFarmName)
                            }
                        }
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = { navController.navigate(Routes.LOGIN) }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    onSkip = { navController.navigate(Routes.HOME) },
                    onAuthSuccess = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToSignUp = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_FARM) {
                AddFarmScreen(
                    onFarmCreated = { name ->
                        scope.launch {
                            farmViewModel.createFarm(name)
                            navController.popBackStack()
                        }
                    }
                )
            }

            composable(Routes.FARM_SWITCHER) {
                val farms by farmViewModel.farms.collectAsStateWithLifecycle()
                val isLoading by farmViewModel.isLoading.collectAsStateWithLifecycle()
                val selectedFarmId by farmViewModel.activeFarmId.collectAsStateWithLifecycle()
                FarmSwitcherScreen(
                    farms = farms,
                    activeFarmId = selectedFarmId,
                    isLoading = isLoading,
                    onBack = { navController.popBackStack() },
                    onSelectFarm = { selected ->
                        farmViewModel.selectFarm(selected)
                        navController.popBackStack()
                    },
                    onDeleteFarm = { farm -> scope.launch { farmViewModel.deleteFarm(farm) } },
                    onAddFarm = { navController.navigate(Routes.ADD_FARM) }
                )
            }

            composable(Routes.HOME) {
                val farms by farmViewModel.farms.collectAsStateWithLifecycle()
                val isLoading by farmViewModel.isLoading.collectAsStateWithLifecycle()
                val farmName by farmViewModel.currentFarmName.collectAsStateWithLifecycle()
                val tasks by farmViewModel.todayTasks.collectAsStateWithLifecycle()
                val allTasks by farmViewModel.tasks.collectAsStateWithLifecycle()
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                val productionRecords by farmViewModel.productionRecords.collectAsStateWithLifecycle()
                val activeHealthCases by farmViewModel.activeHealthCases.collectAsStateWithLifecycle()
                val birthsDueSoon by farmViewModel.birthsDueSoonCount.collectAsStateWithLifecycle()
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val tomorrowStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.DAY_OF_YEAR, 1)
                }.timeInMillis
                val milkToday = productionRecords
                    .filter { it.type == ProductionType.MILK && it.dateMillis in todayStart until tomorrowStart }
                    .sumOf { it.quantity }

                when {
                    // Still resolving the session or the farm list.
                    isLoading -> LoadingState()

                    // A signed-in user with no farm yet: ask for one first.
                    uid != null && farms.isEmpty() -> AddFarmScreen(
                        onFarmCreated = { name ->
                            scope.launch {
                                farmViewModel.createFarm(name)
                                navController.popBackStack()
                            }
                        }
                    )

                    else -> HomeScreen(
                        farmName = farmName,
                        tasks = tasks,
                        activeHealthCaseCount = activeHealthCases.size,
                        birthsDueSoonCount = birthsDueSoon,
                        animalCount = animals.size,
                        taskCount = allTasks.count { !it.completed },
                        todayMilkLitres = milkToday,
                        isGuest = uid == null,
                        onNavigateToAnimals = { navController.navigate(Routes.ANIMALS_LIST) },
                        onNavigateToTasks = { navController.navigate(Routes.TASKS_LIST) },
                        onNavigateToProduction = { navController.navigate(Routes.PRODUCTION_LIST) },
                        onNavigateToBreeding = { navController.navigate(Routes.BREEDING_LIST) },
                        onNavigateToFeeding = { navController.navigate(Routes.FEEDING_LIST) },
                        onNavigateToFinance = { navController.navigate(Routes.FINANCE_LIST) },
                        onNavigateToHealth = { navController.navigate(Routes.HEALTH_LIST) },
                        onNavigateToAi = { navController.navigate(Routes.AI) },
                        onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                        onNavigateToLogin = { navController.navigate(Routes.LOGIN) },
                        onNavigateToSignUp = { navController.navigate(Routes.AUTH) },
                        onToggleTask = { task -> scope.launch { farmViewModel.toggleTaskComplete(task) } },
                        onEditTask = { task -> navController.navigate(Routes.editTask(task.id)) }
                    )
                }
            }

            composable(Routes.SETTINGS) {
                val currentFarmName by farmViewModel.currentFarmName.collectAsStateWithLifecycle()
                SettingsScreen(
                    currentFarmName = currentFarmName,
                    onUpdateFarmName = { newName ->
                        scope.launch { farmViewModel.renameFarm(newName) }
                    },
                    onNavigateToFarmSwitcher = { navController.navigate(Routes.FARM_SWITCHER) },
                    onNavigateToAdmin = { navController.navigate(Routes.ADMIN) },
                    onSignOut = {
                        farmViewModel.signOut()
                        navController.navigate(Routes.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADMIN) {
                val adminPin by farmViewModel.adminPin.collectAsStateWithLifecycle()
                AdminScreen(
                    correctPin = adminPin,
                    onSetPin = { newPin -> scope.launch { farmViewModel.setAdminPin(newPin) } },
                    onOpenFinance = { navController.navigate(Routes.FINANCE_LIST) }
                )
            }

            composable(Routes.ANIMALS_LIST) {
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                AnimalsListScreen(
                    animals = animals,
                    onAddAnimal = { navController.navigate(Routes.ADD_ANIMAL) },
                    onEditAnimal = { id -> navController.navigate(Routes.editAnimal(id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_ANIMAL) {
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                AddEditAnimalScreen(
                    existingAnimals = animals,
                    onSave = { animal ->
                        scope.launch {
                            farmViewModel.saveAnimal(animal)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.EDIT_ANIMAL,
                arguments = listOf(navArgument("animalId") { type = NavType.StringType })
            ) { backStackEntry ->
                val animalId = backStackEntry.arguments?.getString("animalId").orEmpty()
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                val animal = animals.firstOrNull { it.id == animalId }
                AddEditAnimalScreen(
                    animal = animal,
                    existingAnimals = animals,
                    onSave = { updated ->
                        scope.launch {
                            farmViewModel.saveAnimal(updated)
                            navController.popBackStack()
                        }
                    },
                    onDelete = {
                        scope.launch {
                            farmViewModel.deleteAnimal(animalId)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.TASKS_LIST) {
                val tasks by farmViewModel.tasks.collectAsStateWithLifecycle()
                TasksScreen(
                    tasks = tasks,
                    onAddTask = { navController.navigate(Routes.ADD_TASK) },
                    onEditTask = { id -> navController.navigate(Routes.editTask(id)) },
                    onToggleTaskComplete = { task -> scope.launch { farmViewModel.toggleTaskComplete(task) } },
                    onDeleteTask = { id -> scope.launch { farmViewModel.deleteTask(id) } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_TASK) {
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                AddEditTaskScreen(
                    animals = animals,
                    onSave = { task ->
                        scope.launch {
                            farmViewModel.saveTask(task)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.EDIT_TASK,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) { backStackEntry ->
                val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
                val tasks by farmViewModel.tasks.collectAsStateWithLifecycle()
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                val task = tasks.firstOrNull { it.id == taskId }
                AddEditTaskScreen(
                    animals = animals,
                    task = task,
                    onSave = { updated ->
                        scope.launch {
                            farmViewModel.saveTask(updated)
                            navController.popBackStack()
                        }
                    },
                    onDelete = {
                        scope.launch {
                            farmViewModel.deleteTask(taskId)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PRODUCTION_LIST) {
                val records by farmViewModel.productionRecords.collectAsStateWithLifecycle()
                ProductionScreen(
                    records = records,
                    onAddRecord = { type -> navController.navigate("${Routes.ADD_PRODUCTION}/${type.name}") },
                    onDeleteRecord = { id -> scope.launch { farmViewModel.deleteProduction(id) } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "${Routes.ADD_PRODUCTION}/{initialType}",
                arguments = listOf(navArgument("initialType") { type = NavType.StringType })
            ) { entry ->
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                val initialType = runCatching {
                    ProductionType.valueOf(entry.arguments?.getString("initialType") ?: ProductionType.MILK.name)
                }.getOrDefault(ProductionType.MILK)
                AddProductionScreen(
                    animals = animals,
                    initialType = initialType,
                    onSave = { record ->
                        scope.launch {
                            farmViewModel.saveProduction(record)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.BREEDING_LIST) {
                val records by farmViewModel.breedingRecords.collectAsStateWithLifecycle()
                BreedingScreen(
                    records = records,
                    onAddRecord = { navController.navigate(Routes.ADD_BREEDING) },
                    onDeleteRecord = { id -> scope.launch { farmViewModel.deleteBreeding(id) } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_BREEDING) {
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                AddBreedingScreen(
                    animals = animals,
                    onSave = { record ->
                        scope.launch {
                            farmViewModel.saveBreeding(record)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.FEEDING_LIST) {
                val records by farmViewModel.feedingRecords.collectAsStateWithLifecycle()
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                FeedingScreen(
                    records = records,
                    animals = animals,
                    onAddRecord = { navController.navigate(Routes.ADD_FEEDING) },
                    onDeleteRecord = { id -> scope.launch { farmViewModel.deleteFeeding(id) } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_FEEDING) {
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                AddFeedingScreen(
                    animals = animals,
                    onSave = { record ->
                        scope.launch {
                            farmViewModel.saveFeeding(record)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.FINANCE_LIST) {
                val records by farmViewModel.financeRecords.collectAsStateWithLifecycle()
                FinanceScreen(
                    records = records,
                    onAddRecord = { navController.navigate(Routes.ADD_FINANCE) },
                    onDeleteRecord = { id -> scope.launch { farmViewModel.deleteFinance(id) } },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_FINANCE) {
                AddFinanceScreen(
                    onSave = { record ->
                        scope.launch {
                            farmViewModel.saveFinance(record)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.HEALTH_LIST) {
                val cases by farmViewModel.healthCases.collectAsStateWithLifecycle()
                HealthScreen(
                    cases = cases,
                    onAddCase = { navController.navigate(Routes.ADD_HEALTH_CASE) },
                    onCaseDetail = { id -> navController.navigate(Routes.healthCaseDetail(id)) },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.ADD_HEALTH_CASE) {
                val animals by farmViewModel.animals.collectAsStateWithLifecycle()
                AddHealthCaseScreen(
                    animals = animals,
                    onSave = { healthCase ->
                        scope.launch {
                            farmViewModel.saveHealthCase(healthCase)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Routes.HEALTH_CASE_DETAIL,
                arguments = listOf(navArgument("caseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getString("caseId").orEmpty()
                val healthCases by farmViewModel.healthCases.collectAsStateWithLifecycle()
                val healthCase = healthCases.firstOrNull { it.id == caseId }
                HealthCaseDetailScreen(
                    healthCase = healthCase,
                    onUpdateStatus = { status ->
                        scope.launch { farmViewModel.updateHealthCaseStatus(caseId, status) }
                    },
                    onDelete = {
                        scope.launch {
                            farmViewModel.deleteHealthCase(caseId)
                            navController.popBackStack()
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.AI) {
                val messages by aiViewModel.messages.collectAsStateWithLifecycle()
                val isLoading by aiViewModel.isLoading.collectAsStateWithLifecycle()
                val savedChats by aiViewModel.savedChats.collectAsStateWithLifecycle()
                val currentFarmName by farmViewModel.currentFarmName.collectAsStateWithLifecycle()
                AiTabScreen(
                    farmName = currentFarmName,
                    messages = messages,
                    savedChats = savedChats,
                    isLoading = isLoading,
                    onSendMessage = { prompt, attachment -> aiViewModel.askAi(prompt, attachment) },
                    onStartNewChat = { aiViewModel.startNewChat() },
                    onOpenSavedChat = { id -> aiViewModel.openSavedChat(id) },
                    onDeleteSavedChat = { id -> aiViewModel.deleteSavedChat(id) },
                    onBack = { navController.popBackStack() }
                )
            }
        }
      }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/** The shared "we are still loading" indicator. */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
