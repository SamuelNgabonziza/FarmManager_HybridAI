package com.samuel.farmos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.samuel.farmos.data.HealthCaseStatus
import com.samuel.farmos.ui.screens.*
import com.samuel.farmos.ui.viewmodel.FarmAiViewModel
import com.samuel.farmos.ui.viewmodel.FarmViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val farmViewModel: FarmViewModel = viewModel(factory = FarmViewModel.Factory)
    val aiViewModel: FarmAiViewModel = viewModel()

    val uid by farmViewModel.uid.collectAsStateWithLifecycle()
    val activeFarmId by farmViewModel.activeFarmId.collectAsStateWithLifecycle()

    // The assistant must operate on the same farm the rest of the app is using.
    LaunchedEffect(uid, activeFarmId) {
        aiViewModel.setFarmContext(uid, activeFarmId)
    }

    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onFinished = { signedIn ->
                    navController.navigate(if (signedIn) Routes.HOME else Routes.AUTH) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.AUTH) {
            AuthScreen(
                onSkip = { navController.navigate(Routes.HOME) },
                onAuthSuccess = { initialFarmName ->
                    scope.launch {
                        if (initialFarmName != null) {
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
                onAuthSuccess = { _ ->
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
            val activeFarmId by farmViewModel.activeFarmId.collectAsStateWithLifecycle()
            FarmSwitcherScreen(
                farms = farms,
                activeFarmId = activeFarmId,
                isLoading = isLoading,
                onSelectFarm = { selected ->
                    farmViewModel.selectFarm(selected)
                    navController.popBackStack()
                },
                onAddFarm = { navController.navigate(Routes.ADD_FARM) }
            )
        }

        composable(Routes.HOME) {
            val farms by farmViewModel.farms.collectAsStateWithLifecycle()
            val isLoading by farmViewModel.isLoading.collectAsStateWithLifecycle()
            val farmName by farmViewModel.currentFarmName.collectAsStateWithLifecycle()
            val tasks by farmViewModel.todayTasks.collectAsStateWithLifecycle()
            val activeHealthCases by farmViewModel.activeHealthCases.collectAsStateWithLifecycle()
            val birthsDueSoon by farmViewModel.birthsDueSoonCount.collectAsStateWithLifecycle()

            if (uid != null && !isLoading && farms.isEmpty()) {
                // A brand-new account has no farm yet — ask for one before showing anything else.
                AddFarmScreen(
                    onFarmCreated = { name ->
                        scope.launch {
                            farmViewModel.createFarm(name)
                            navController.popBackStack()
                        }
                    }
                )
            } else {
                HomeScreen(
                    farmName = farmName,
                    tasks = tasks,
                    activeHealthCaseCount = activeHealthCases.size,
                    birthsDueSoonCount = birthsDueSoon,
                    onNavigateToAnimals = { navController.navigate(Routes.ANIMALS_LIST) },
                    onNavigateToTasks = { navController.navigate(Routes.TASKS_LIST) },
                    onNavigateToProduction = { navController.navigate(Routes.PRODUCTION_LIST) },
                    onNavigateToBreeding = { navController.navigate(Routes.BREEDING_LIST) },
                    onNavigateToFeeding = { navController.navigate(Routes.FEEDING_LIST) },
                    onNavigateToFinance = { navController.navigate(Routes.FINANCE_LIST) },
                    onNavigateToHealth = { navController.navigate(Routes.HEALTH_LIST) },
                    onNavigateToAi = { navController.navigate(Routes.AI) },
                    onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
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
                }
            )
        }

        composable(Routes.ADMIN) {
            val adminPin by farmViewModel.adminPin.collectAsStateWithLifecycle()
            AdminScreen(
                correctPin = adminPin,
                onSetPin = { newPin -> scope.launch { farmViewModel.setAdminPin(newPin) } }
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
            AddEditAnimalScreen(
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
            val animalId = backStackEntry.arguments?.getString("animalId") ?: ""
            val animals by farmViewModel.animals.collectAsStateWithLifecycle()
            val animal = animals.firstOrNull { it.id == animalId }
            AddEditAnimalScreen(
                animal = animal,
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
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_TASK) {
            AddEditTaskScreen(
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
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            val tasks by farmViewModel.tasks.collectAsStateWithLifecycle()
            val task = tasks.firstOrNull { it.id == taskId }
            AddEditTaskScreen(
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
                onAddRecord = { navController.navigate(Routes.ADD_PRODUCTION) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_PRODUCTION) {
            AddProductionScreen(
                onSave = { record ->
                    scope.launch { farmViewModel.saveProduction(record); navController.popBackStack() }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.BREEDING_LIST) {
            val records by farmViewModel.breedingRecords.collectAsStateWithLifecycle()
            BreedingScreen(
                records = records,
                onAddRecord = { navController.navigate(Routes.ADD_BREEDING) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_BREEDING) {
            AddBreedingScreen(
                onSave = { record ->
                    scope.launch { farmViewModel.saveBringing(record); navController.popBackStack() }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FEEDING_LIST) {
            val records by farmViewModel.feedingRecords.collectAsStateWithLifecycle()
            FeedingScreen(
                records = records,
                onAddRecord = { navController.navigate(Routes.ADD_FEEDING) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_FEEDING) {
            AddFeedingScreen(
                onSave = { record ->
                    scope.launch { farmViewModel.saveFeeding(record); navController.popBackStack() }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FINANCE_LIST) {
            val records by farmViewModel.financeRecords.collectAsStateWithLifecycle()
            FinanceScreen(
                records = records,
                onAddRecord = { navController.navigate(Routes.ADD_FINANCE) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADD_FINANCE) {
            AddFinanceScreen(
                onSave = { record ->
                    scope.launch { farmViewModel.saveFinance(record); navController.popBackStack() }
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
            AddHealthCaseScreen(
                onSave = { healthCase ->
                    scope.launch { farmViewModel.saveHealthCase(healthCase); navController.popBackStack() }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            Routes.HEALTH_CASE_DETAIL,
            arguments = listOf(navArgument("caseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val caseId = backStackEntry.arguments?.getString("caseId") ?: ""
            val healthCases by farmViewModel.healthCases.collectAsStateWithLifecycle()
            val healthCase = healthCases.firstOrNull { it.id == caseId }
            HealthCaseDetailScreen(
                healthCase = healthCase,
                onUpdateStatus = { status ->
                    scope.launch { farmViewModel.updateHealthCaseStatus(caseId, status) }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AI) {
            val messages by aiViewModel.messages.collectAsStateWithLifecycle()
            val isLoading by aiViewModel.isLoading.collectAsStateWithLifecycle()
            AiTabScreen(
                messages = messages,
                isLoading = isLoading,
                onSendMessage = { prompt, attachment -> aiViewModel.askAi(prompt, attachment) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
