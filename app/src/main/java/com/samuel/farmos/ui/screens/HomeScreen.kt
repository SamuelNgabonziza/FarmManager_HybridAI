package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.FinanceRecord
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.ProductionRecord
import com.samuel.farmos.data.Task
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    farmName: String?,
    tasks: List<Task>,
    activeHealthCaseCount: Int,
    birthsDueSoonCount: Int,
    animalCount: Int = 0,
    taskCount: Int = tasks.count { !it.completed },
    todayMilkLitres: Double = 0.0,
    isGuest: Boolean = false,
    onNavigateToAnimals: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToProduction: () -> Unit,
    onNavigateToBreeding: () -> Unit,
    onNavigateToFeeding: () -> Unit,
    onNavigateToFinance: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToAi: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onToggleTask: (Task) -> Unit = {},
    onEditTask: (Task) -> Unit = {},
    animals: List<Animal> = emptyList(),
    onQuickSaveMilk: (ProductionRecord) -> Unit = {},
    onQuickSaveHealth: (HealthCase) -> Unit = {},
    onQuickSaveExpense: (FinanceRecord) -> Unit = {},
    onQuickSaveTask: (Task) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FarmLogo(36.dp)
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text(
                                text = if (isGuest) "FarmOS" else farmName?.takeIf { it.isNotBlank() } ?: "FarmOS",
                                fontWeight = FontWeight.Bold,
                                color = FarmUi.green
                            )
                            Text(
                                text = if (isGuest) "Modern Farm Management" else "Farm Overview",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FarmUi.textMuted
                            )
                        }
                    }
                },
                actions = {
                    if (isGuest) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = FarmUi.green,
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clickable(onClick = onNavigateToLogin)
                        ) {
                            Text(
                                text = "Log in",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = FarmUi.green)
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                WelcomeCard(
                    farmName = farmName,
                    isGuest = isGuest,
                    onGetStarted = onNavigateToSignUp
                )
            }

            item {
                Text(
                    text = "Your farm's latest numbers",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.green
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Pets,
                        value = animalCount.toString(),
                        label = "Animals",
                        iconColor = FarmUi.teal,
                        iconBackground = FarmUi.iconTeal,
                        borderColor = FarmUi.cardBorder
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Assignment,
                        value = taskCount.toString(),
                        label = "Tasks",
                        iconColor = FarmUi.green,
                        iconBackground = FarmUi.iconMint,
                        borderColor = FarmUi.cardBorder
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.TrendingUp,
                        value = String.format(Locale.US, "%.1f L", todayMilkLitres),
                        label = "Milk",
                        iconColor = FarmUi.orange,
                        iconBackground = FarmUi.iconWarm,
                        borderColor = FarmUi.cardBorder
                    )
                }
            }

            item {
                Text(
                    text = "Farm tools",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FarmUi.green
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeToolCard(
                        title = "Animals",
                        subtitle = "Livestock & tags",
                        icon = Icons.Default.Pets,
                        iconColor = FarmUi.teal,
                        iconBackground = FarmUi.iconTeal,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToAnimals,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Today's tasks",
                        subtitle = "Schedules & alerts",
                        icon = Icons.Default.Assignment,
                        iconColor = FarmUi.teal,
                        iconBackground = FarmUi.iconTeal,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToTasks,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeToolCard(
                        title = "Production",
                        subtitle = "Milk & weights",
                        icon = Icons.Default.ShowChart,
                        iconColor = FarmUi.orange,
                        iconBackground = FarmUi.iconWarm,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToProduction,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Breeding",
                        subtitle = "Matings & due dates",
                        icon = Icons.Default.CalendarMonth,
                        iconColor = FarmUi.purple,
                        iconBackground = FarmUi.iconLavender,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToBreeding,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeToolCard(
                        title = "Feeding",
                        subtitle = "Rations & feeds",
                        icon = Icons.Default.Restaurant,
                        iconColor = FarmUi.orange,
                        iconBackground = FarmUi.iconPeach,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToFeeding,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Health",
                        subtitle = "Cases & vaccines",
                        icon = Icons.Default.Favorite,
                        iconColor = FarmUi.coral,
                        iconBackground = FarmUi.iconCoral,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToHealth,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeToolCard(
                        title = "Farm records",
                        subtitle = "Finances & sales",
                        icon = Icons.Default.TrendingUp,
                        iconColor = FarmUi.navy,
                        iconBackground = FarmUi.iconBlue,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToFinance,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Ask AI",
                        subtitle = "Symptom diagnosis",
                        icon = Icons.Default.ArrowForward,
                        iconColor = FarmUi.orange,
                        iconBackground = FarmUi.iconWarm,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToAi,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HomeToolCard(
                        title = "Settings",
                        subtitle = "Farm & password",
                        icon = Icons.Default.Settings,
                        iconColor = FarmUi.textMuted,
                        iconBackground = FarmUi.iconMint,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WelcomeCard(
    farmName: String?,
    isGuest: Boolean,
    onGetStarted: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF075C43), Color(0xFF009D8D))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = if (isGuest) "Welcome to FarmOS" else "Welcome back 👋",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGuest) "Grow smarter, every day" else farmName?.takeIf { it.isNotBlank() } ?: "Your farm",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isGuest) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        modifier = Modifier.clickable(onClick = onGetStarted)
                    ) {
                        Text(
                            text = "Get Started Free",
                            color = Color(0xFF075C43),
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Surface(
                color = Color.White.copy(alpha = 0.16f),
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isGuest) "🌿" else "🏡",
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    modifier: Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    iconColor: Color,
    iconBackground: Color,
    borderColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(iconBackground, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FarmUi.ink)
            Text(label, style = MaterialTheme.typography.bodyMedium, color = FarmUi.textMuted)
        }
    }
}

@Composable
private fun HomeToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    iconBackground: Color,
    outline: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
        border = BorderStroke(1.dp, outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconBackground, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FarmUi.ink)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    FarmOsTheme {
        HomeScreen(
            farmName = "Green Valley Farm",
            tasks = listOf(Task(id = "1", title = "Feed the calves", dueDateMillis = System.currentTimeMillis())),
            activeHealthCaseCount = 1,
            birthsDueSoonCount = 2,
            animalCount = 18,
            taskCount = 3,
            todayMilkLitres = 26.5,
            onNavigateToAnimals = {},
            onNavigateToTasks = {},
            onNavigateToProduction = {},
            onNavigateToBreeding = {},
            onNavigateToFeeding = {},
            onNavigateToFinance = {},
            onNavigateToHealth = {},
            onNavigateToAi = {},
            onNavigateToSettings = {},
            onNavigateToLogin = {},
            onNavigateToSignUp = {}
        )
    }
}
