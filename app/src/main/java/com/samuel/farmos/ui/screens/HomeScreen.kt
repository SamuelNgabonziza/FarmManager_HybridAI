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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
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
import com.samuel.farmos.data.Task
import com.samuel.farmos.ui.theme.FarmOsTheme

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
    onNavigateToSignUp: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FarmLogo(46.dp)
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
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Log in", color = FarmUi.green, fontWeight = FontWeight.Bold)
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
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                WelcomeCard(farmName = farmName, isGuest = isGuest, onGetStarted = onNavigateToSignUp)
            }

            item {
                Text(
                    text = "Your farm’s latest numbers",
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
                        iconBackground = FarmUi.surfaceTeal,
                        borderColor = FarmUi.teal.copy(alpha = 0.28f)
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Assignment,
                        value = taskCount.toString(),
                        label = "Tasks",
                        iconColor = FarmUi.green,
                        iconBackground = FarmUi.surfaceMint,
                        borderColor = FarmUi.green.copy(alpha = 0.24f)
                    )
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ShowChart,
                        value = String.format("%.1f L", todayMilkLitres),
                        label = "Milk",
                        iconColor = FarmUi.orange,
                        iconBackground = FarmUi.surfaceWarm,
                        borderColor = FarmUi.orange.copy(alpha = 0.28f)
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
                        iconBackground = FarmUi.surfaceTeal,
                        outline = FarmUi.teal.copy(alpha = 0.34f),
                        onClick = onNavigateToAnimals,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Today’s tasks",
                        subtitle = "Schedules & alerts",
                        icon = Icons.Default.Assignment,
                        iconColor = FarmUi.teal,
                        iconBackground = FarmUi.surfaceTeal,
                        outline = FarmUi.teal.copy(alpha = 0.34f),
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
                        iconBackground = FarmUi.surfaceWarm,
                        outline = FarmUi.orange.copy(alpha = 0.34f),
                        onClick = onNavigateToProduction,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Breeding",
                        subtitle = "Matings & due dates",
                        icon = Icons.Default.ShowChart,
                        iconColor = FarmUi.purple,
                        iconBackground = FarmUi.surfaceLavender,
                        outline = FarmUi.purple.copy(alpha = 0.34f),
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
                        iconBackground = FarmUi.surfacePeach,
                        outline = FarmUi.orange.copy(alpha = 0.34f),
                        onClick = onNavigateToFeeding,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Health",
                        subtitle = "Cases & vaccines",
                        icon = Icons.Default.Favorite,
                        iconColor = FarmUi.coral,
                        iconBackground = FarmUi.surfaceCoral,
                        outline = FarmUi.coral.copy(alpha = 0.34f),
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
                        iconBackground = FarmUi.surfaceBlue,
                        outline = FarmUi.navy.copy(alpha = 0.34f),
                        onClick = onNavigateToFinance,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Ask AI",
                        subtitle = "Farm advice",
                        icon = Icons.Default.Favorite,
                        iconColor = FarmUi.orange,
                        iconBackground = FarmUi.surfaceWarm,
                        outline = FarmUi.orange.copy(alpha = 0.34f),
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
                        iconBackground = FarmUi.surfaceMint,
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
private fun WelcomeCard(farmName: String?, isGuest: Boolean, onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    listOf(Color(0xFF075C43), Color(0xFF009D8D))
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (isGuest) "Welcome to FarmOS" else "Welcome back 👋",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGuest) "Grow smarter, every day" else farmName?.takeIf { it.isNotBlank() } ?: "Your farm",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            if (isGuest) {
                TextButton(onClick = onGetStarted) { Text("Get Started Free", color = Color.White, fontWeight = FontWeight.Bold) }
            } else {
                Text(text = "🏡", style = MaterialTheme.typography.headlineLarge)
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
        shape = RoundedCornerShape(18.dp),
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
                    .size(36.dp)
                    .background(iconBackground, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = FarmUi.ink)
            Text(label, style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
        border = BorderStroke(1.dp, outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBackground, RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(25.dp))
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
