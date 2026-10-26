package com.samuel.farmos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samuel.farmos.ui.screens.FarmUi

enum class QuickActionType {
    MILK,
    HEALTH,
    EXPENSE,
    TASK,
    VOICE
}

@Composable
fun QuickActionSpeedDial(
    onSelectAction: (QuickActionType) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 135f else 0f,
        animationSpec = spring(dampingRatio = 0.65f),
        label = "fabRotation"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // Scrim when expanded
        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { expanded = false }
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(bottom = 20.dp, end = 20.dp)
        ) {
            // Speed dial actions list
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                exit = fadeOut(tween(150)) + scaleOut(tween(150))
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpeedDialItem(
                        label = "Voice Log",
                        icon = Icons.Default.Mic,
                        containerColor = FarmUi.teal,
                        contentColor = if (isSystemInDarkTheme()) Color(0xFF003730) else Color.White,
                        onClick = {
                            expanded = false
                            onSelectAction(QuickActionType.VOICE)
                        }
                    )
                    SpeedDialItem(
                        label = "Quick Milk Log",
                        icon = Icons.Default.Opacity,
                        containerColor = FarmUi.green,
                        contentColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White,
                        onClick = {
                            expanded = false
                            onSelectAction(QuickActionType.MILK)
                        }
                    )
                    SpeedDialItem(
                        label = "Report Illness",
                        icon = Icons.Default.Favorite,
                        containerColor = FarmUi.coral,
                        contentColor = if (isSystemInDarkTheme()) Color(0xFF4C1821) else Color.White,
                        onClick = {
                            expanded = false
                            onSelectAction(QuickActionType.HEALTH)
                        }
                    )
                    SpeedDialItem(
                        label = "Quick Expense",
                        icon = Icons.Default.AttachMoney,
                        containerColor = FarmUi.orange,
                        contentColor = if (isSystemInDarkTheme()) Color(0xFF3D2005) else Color.White,
                        onClick = {
                            expanded = false
                            onSelectAction(QuickActionType.EXPENSE)
                        }
                    )
                    SpeedDialItem(
                        label = "New Task",
                        icon = Icons.AutoMirrored.Filled.Assignment,
                        containerColor = FarmUi.navy,
                        contentColor = if (isSystemInDarkTheme()) Color(0xFF12243A) else Color.White,
                        onClick = {
                            expanded = false
                            onSelectAction(QuickActionType.TASK)
                        }
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }

            // Main Floating Action Button
            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = FarmUi.green,
                contentColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (expanded) "Close actions" else "Quick actions",
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun SpeedDialItem(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = FarmUi.surface,
            shadowElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, FarmUi.cardBorder),
            modifier = Modifier.clickable(onClick = onClick)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = FarmUi.ink,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            contentColor = contentColor,
            shape = CircleShape,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
    }
}
