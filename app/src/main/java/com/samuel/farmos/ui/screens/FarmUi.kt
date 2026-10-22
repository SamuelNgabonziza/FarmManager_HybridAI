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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import com.samuel.farmos.data.Task
import com.samuel.farmos.ui.theme.FarmOsTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.isSystemInDarkTheme
import com.samuel.farmos.R

object FarmUi {
    val cardShape = RoundedCornerShape(18.dp)
    val fieldShape = RoundedCornerShape(12.dp)
    val pillShape = RoundedCornerShape(50.dp)

    val background: Color @Composable get() = MaterialTheme.colorScheme.background
    val surface: Color @Composable get() = MaterialTheme.colorScheme.surface
    val surfaceWarm: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF403A2B) else Color.White
    val surfaceMint: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2D4033) else Color.White
    val surfaceTeal: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF2A3E3A) else Color.White
    val surfaceBlue: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF303A49) else Color.White
    val surfaceCoral: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF423238) else Color.White
    val surfaceLavender: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF3B3547) else Color.White
    val surfacePeach: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF44372F) else Color.White
    val surfaceSky: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF303E48) else Color.White

    val green: Color @Composable get() = MaterialTheme.colorScheme.primary
    val deepGreen: Color @Composable get() = MaterialTheme.colorScheme.primary
    val greenGlow: Color @Composable get() = MaterialTheme.colorScheme.secondary
    val ink: Color @Composable get() = MaterialTheme.colorScheme.onSurface
    val textMuted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val navy: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF99B9F2) else Color(0xFF3766A4)
    val teal: Color @Composable get() = MaterialTheme.colorScheme.secondary
    val amber: Color @Composable get() = MaterialTheme.colorScheme.tertiary
    val coral: Color @Composable get() = MaterialTheme.colorScheme.error
    val purple: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFC2A5F3) else Color(0xFF7950B8)
    val orange: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF0B04F) else Color(0xFFB87513)
    val cardBorder: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val cardBorderStrong: Color @Composable get() = MaterialTheme.colorScheme.outline

    @OptIn(ExperimentalMaterial3Api::class)
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
fun FarmLogo(size: androidx.compose.ui.unit.Dp = 56.dp) {
    androidx.compose.material3.Surface(shape = CircleShape, color = if (isSystemInDarkTheme()) Color(0xFF284A39) else Color(0xFFDFF2E4)) {
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Image(painter = painterResource(R.drawable.ic_farm_wheat), contentDescription = "FarmOS wheat logo", modifier = Modifier.size(size * 0.68f))
        }
    }
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
