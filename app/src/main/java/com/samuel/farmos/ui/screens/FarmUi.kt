package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samuel.farmos.data.Task
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FarmUi {
    val cardShape = RoundedCornerShape(22.dp)
    val fieldShape = RoundedCornerShape(14.dp)
    val pillShape = RoundedCornerShape(50.dp)

    val background: Color @Composable get() = MaterialTheme.colorScheme.background
    val surface: Color @Composable get() = MaterialTheme.colorScheme.surface
    val surfaceWarm: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
    val surfaceMint: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val surfaceTeal: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val surfaceBlue: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val surfaceCoral: Color @Composable get() = MaterialTheme.colorScheme.errorContainer
    val surfaceLavender: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
    val surfacePeach: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val surfaceSky: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer

    val green: Color @Composable get() = MaterialTheme.colorScheme.primary
    val deepGreen: Color @Composable get() = MaterialTheme.colorScheme.primary
    val greenGlow: Color @Composable get() = MaterialTheme.colorScheme.secondary
    val ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val textMuted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val navy: Color @Composable get() = MaterialTheme.colorScheme.primary
    val teal: Color @Composable get() = MaterialTheme.colorScheme.secondary
    val amber: Color @Composable get() = MaterialTheme.colorScheme.tertiary
    val coral: Color @Composable get() = MaterialTheme.colorScheme.error
    val purple: Color @Composable get() = MaterialTheme.colorScheme.tertiary
    val orange: Color @Composable get() = MaterialTheme.colorScheme.tertiary
    val cardBorder: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val cardBorderStrong: Color @Composable get() = MaterialTheme.colorScheme.outline

    @Composable
    fun topAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = green
    )

    @Composable
    fun buttonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
    )

    @Composable
    fun secondaryButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondary,
        contentColor = MaterialTheme.colorScheme.onSecondary
    )

    @Composable
    fun outlinedButtonColors(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )

    @Composable
    fun textFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        focusedContainerColor = MaterialTheme.colorScheme.surface,
        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
fun TodayOnFarmCard(
    tasks: List<Task>,
    activeHealthCaseCount: Int,
    birthsDueSoonCount: Int,
    onHealthClick: () -> Unit,
    onBreedingClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, FarmUi.green.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Today on the farm",
                color = FarmUi.deepGreen,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            if (tasks.isEmpty() && activeHealthCaseCount == 0 && birthsDueSoonCount == 0) {
                Text(
                    text = "No urgent items. You’re all caught up.",
                    color = FarmUi.textMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            tasks.take(3).forEach { task ->
                val dateStr = if (task.dueDateMillis < System.currentTimeMillis()) "Overdue" else "Due " + SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(task.dueDateMillis))
                Text(
                    text = "• ${task.title} · $dateStr",
                    color = FarmUi.ink,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (activeHealthCaseCount > 0) {
                val plural = if (activeHealthCaseCount == 1) "" else "s"
                Text(
                    text = "$activeHealthCaseCount active health case$plural",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onHealthClick)
                        .padding(vertical = 4.dp),
                    color = FarmUi.ink,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (birthsDueSoonCount > 0) {
                val plural = if (birthsDueSoonCount == 1) "" else "s"
                Text(
                    text = "$birthsDueSoonCount expected birth$plural in the next 7 days",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onBreedingClick)
                        .padding(vertical = 4.dp),
                    color = FarmUi.ink,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
