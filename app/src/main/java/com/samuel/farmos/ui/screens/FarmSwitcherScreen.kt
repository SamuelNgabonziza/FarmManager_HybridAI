package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Farm
import com.samuel.farmos.ui.components.FarmDeleteButton
import com.samuel.farmos.ui.components.FarmEmptyState
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmSwitcherScreen(
    farms: List<Farm>,
    activeFarmId: String?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onSelectFarm: (Farm) -> Unit,
    onDeleteFarm: (Farm) -> Unit,
    onAddFarm: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Switch Farm", fontWeight = FontWeight.Bold, color = FarmUi.green)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddFarm,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Farm", fontWeight = FontWeight.Bold) },
                containerColor = FarmUi.green,
                contentColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White,
                shape = RoundedCornerShape(24.dp)
            )
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = FarmUi.background
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FarmUi.green)
                }
            } else if (farms.isEmpty()) {
                FarmEmptyState(
                    icon = Icons.Default.Home,
                    title = "No farms yet",
                    message = "Create a farm to start recording animals, tasks, production and finances.",
                    actionLabel = "Add Farm",
                    onAction = onAddFarm
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Select which farm you want to manage or switch to. All records and livestock belong to the active farm.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = FarmUi.textMuted
                        )
                    }
                    items(farms, key = { it.id }) { farm ->
                        val isActive = farm.id == activeFarmId
                        val activeCardBg = if (isSystemInDarkTheme()) Color(0xFF254B33) else Color(0xFFDFF8EC)
                        val activeIconBg = if (isSystemInDarkTheme()) Color(0xFF326543) else Color(0xFFBCEECE)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = Role.Button, onClick = { onSelectFarm(farm) }),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) activeCardBg else FarmUi.surface
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isActive) FarmUi.green else FarmUi.cardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isActive) activeIconBg else FarmUi.iconMint
                                ) {
                                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Home,
                                            contentDescription = null,
                                            tint = FarmUi.green,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = farm.farmName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = FarmUi.ink
                                    )
                                    if (isActive) {
                                        Text(
                                            "✓ Active Farm",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = FarmUi.green,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                FarmDeleteButton(
                                    description = "${farm.farmName} farm",
                                    onDelete = { onDeleteFarm(farm) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FarmSwitcherScreenPreview() {
    FarmOsTheme {
        FarmSwitcherScreen(
            farms = listOf(
                Farm(id = "1", farmName = "Green Valley Farm"),
                Farm(id = "2", farmName = "Sunny Hills Ranch")
            ),
            activeFarmId = "1",
            isLoading = false,
            onBack = {},
            onSelectFarm = {},
            onDeleteFarm = {},
            onAddFarm = {}
        )
    }
}
