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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.samuel.farmos.data.Animal
import com.samuel.farmos.data.FinanceRecord
import com.samuel.farmos.data.HealthCase
import com.samuel.farmos.data.ProductionRecord
import com.samuel.farmos.data.Task
import com.samuel.farmos.ui.theme.FarmOsTheme
import kotlinx.coroutines.launch
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
    onQuickSaveTask: (Task) -> Unit = {},
    onAskAiWithPrompt: (String) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()

    val navigateFromDrawer: (() -> Unit) -> Unit = { action ->
        drawerScope.launch { drawerState.close() }
        action()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = FarmUi.surface,
                drawerContentColor = FarmUi.ink
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FarmLogo(40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = farmName?.takeIf { it.isNotBlank() } ?: "FarmOS",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FarmUi.green
                            )
                            Text(
                                text = "Firebase AI Logic • Hybrid",
                                style = MaterialTheme.typography.bodySmall,
                                color = FarmUi.textMuted
                            )
                        }
                    }
                }
                HorizontalDivider(color = FarmUi.cardBorder)
                Spacer(Modifier.height(8.dp))

                val drawerItems = listOf(
                    Triple("Farm Overview", Icons.Default.Home) { },
                    Triple("FarmOS Hybrid AI", Icons.Default.AutoAwesome, onNavigateToAi),
                    Triple("Animals", Icons.Default.Pets, onNavigateToAnimals),
                    Triple("Today's Tasks", Icons.Default.Assignment, onNavigateToTasks),
                    Triple("Production & Yields", Icons.Default.ShowChart, onNavigateToProduction),
                    Triple("Breeding", Icons.Default.CalendarMonth, onNavigateToBreeding),
                    Triple("Feeding", Icons.Default.Restaurant, onNavigateToFeeding),
                    Triple("Health", Icons.Default.Favorite, onNavigateToHealth),
                    Triple("Farm Finances", Icons.Default.TrendingUp, onNavigateToFinance),
                    Triple("Settings", Icons.Default.Settings, onNavigateToSettings)
                )
                drawerItems.forEachIndexed { idx, (label, icon, action) ->
                    NavigationDrawerItem(
                        label = { Text(label, fontWeight = FontWeight.SemiBold) },
                        selected = idx == 0,
                        onClick = { navigateFromDrawer(action) },
                        icon = { Icon(icon, contentDescription = null) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = FarmUi.iconMint,
                            selectedIconColor = FarmUi.green,
                            selectedTextColor = FarmUi.green,
                            unselectedIconColor = FarmUi.textMuted,
                            unselectedTextColor = FarmUi.ink
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }
            }
        }
    ) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { drawerScope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Open menu", tint = FarmUi.green)
                    }
                },
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
                .padding(innerPadding)
                .clipToBounds(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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

            if (!isGuest) {
                item {
                    val insightPrompt = when {
                        activeHealthCaseCount > 0 ->
                            "Review my $activeHealthCaseCount active livestock health case(s) and give immediate care, biosecurity, and vet follow-up advice."
                        birthsDueSoonCount > 0 ->
                            "I have $birthsDueSoonCount livestock birth(s) due within 7 days. Give me a calving/kidding preparation checklist."
                        taskCount > 0 ->
                            "Review my $taskCount open farm tasks today and prioritize what should be done first."
                        else ->
                            "Analyze my current farm records and suggest ways to improve milk yield, feed efficiency, and profitability."
                    }
                    val insightLabel = when {
                        activeHealthCaseCount > 0 -> "🩺 $activeHealthCaseCount active health case(s) — Tap for AI vet advice"
                        birthsDueSoonCount > 0 -> "🐄 $birthsDueSoonCount birth(s) due soon — Tap for AI prep plan"
                        taskCount > 0 -> "📋 $taskCount open task(s) — Tap for AI priority plan"
                        else -> "✨ Tap for proactive FarmOS Hybrid AI insights"
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = Role.Button) { onAskAiWithPrompt(insightPrompt) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = FarmUi.surfaceMint),
                        border = BorderStroke(1.dp, FarmUi.green.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(FarmUi.iconMint, RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = FarmUi.green, modifier = Modifier.size(18.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hybrid AI Insight",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FarmUi.green,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = insightLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FarmUi.ink,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
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
                        icon = Icons.Default.Pets,
                        iconColor = FarmUi.teal,
                        iconBackground = FarmUi.iconTeal,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToAnimals,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Tasks",
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
                        icon = Icons.Default.ShowChart,
                        iconColor = FarmUi.orange,
                        iconBackground = FarmUi.iconWarm,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToProduction,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Breeding",
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
                        icon = Icons.Default.Restaurant,
                        iconColor = FarmUi.orange,
                        iconBackground = FarmUi.iconPeach,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToFeeding,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "Health",
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
                        title = "Records",
                        icon = Icons.Default.TrendingUp,
                        iconColor = FarmUi.navy,
                        iconBackground = FarmUi.iconBlue,
                        outline = FarmUi.cardBorder,
                        onClick = onNavigateToFinance,
                        modifier = Modifier.weight(1f)
                    )
                    HomeToolCard(
                        title = "FarmOS Hybrid AI",
                        icon = Icons.Default.AutoAwesome,
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
                shape = RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Spacer(modifier = Modifier.height(2.dp))
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(iconBackground, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(14.dp))
            }
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = FarmUi.ink)
            Text(label, style = MaterialTheme.typography.bodySmall, color = FarmUi.textMuted)
        }
    }
}

@Composable
private fun HomeToolCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    iconBackground: Color,
    outline: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
        border = BorderStroke(1.dp, outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(iconBackground, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = FarmUi.ink,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
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
