package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samuel.farmos.R
import com.samuel.farmos.data.Task
import com.samuel.farmos.ui.theme.FarmGreen
import com.samuel.farmos.ui.theme.FarmGreenDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FarmUi {
    val cardShape = RoundedCornerShape(18.dp)
    val fieldShape = RoundedCornerShape(12.dp)
    val pillShape = RoundedCornerShape(50.dp)

    val background: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1E2822) else Color(0xFFF7FBF5)
    val surface: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF25332A) else Color.White
    val surfaceWarm: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF383020) else Color.White
    val surfaceMint: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF223829) else Color.White
    val surfaceTeal: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1D3530) else Color.White
    val surfaceBlue: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF202E3D) else Color.White
    val surfaceCoral: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF3B2225) else Color.White
    val surfaceLavender: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF30243C) else Color.White
    val surfacePeach: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF38291F) else Color.White
    val surfaceSky: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1F323E) else Color.White

    val iconMint: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF284833) else Color(0xFFE2FFF0)
    val iconTeal: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF204840) else Color(0xFFD9F7F0)
    val iconWarm: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF4A3C20) else Color(0xFFFFF2CD)
    val iconBlue: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF233954) else Color(0xFFDDEAFF)
    val iconCoral: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF4A2528) else Color(0xFFFFE5EA)
    val iconLavender: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF382552) else Color(0xFFF0E5FF)
    val iconPeach: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF48301E) else Color(0xFFFFEAD7)
    val settingsIconMint: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF284833) else Color(0xFFDFF2E4)
    val settingsIconWarm: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF4A3C20) else Color(0xFFFFF0C8)

    val taskCard: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF25332A) else Color.White
    val taskCardBorder: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF384B3D) else Color(0xFFE2EBE2)
    val dialogSurface: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF25332A) else Color.White

    val green: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF26733A)
    val deepGreen: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else Color(0xFF1A5A29)
    val greenGlow: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF99E5B4) else Color(0xFF34A853)
    val ink: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFF1F5F1) else Color(0xFF1B241D)
    val textMuted: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFB5C4B7) else Color(0xFF5E6F62)
    val navy: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF99B9F2) else Color(0xFF3766A4)
    val teal: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF52D3C0) else Color(0xFF008D84)
    val amber: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFFA94D) else Color(0xFFD28A20)
    val coral: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFFAAA6) else Color(0xFFD32F2F)
    val purple: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFC7A8FF) else Color(0xFF7E57C2)
    val orange: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFFFFA94D) else Color(0xFFE67E22)
    val cardBorder: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF384B3D) else Color(0xFFE2EBE2)
    val cardBorderStrong: Color @Composable get() = if (isSystemInDarkTheme()) Color(0xFF4D6654) else Color(0xFFBFCFBF)

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun topAppBarColors(): TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = green
    )

    @Composable
    fun buttonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (isSystemInDarkTheme()) Color(0xFF80E3A7) else FarmGreen,
        contentColor = if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White,
        disabledContainerColor = (if (isSystemInDarkTheme()) Color(0xFF80E3A7) else FarmGreen).copy(alpha = 0.38f),
        disabledContentColor = (if (isSystemInDarkTheme()) Color(0xFF143820) else Color.White).copy(alpha = 0.7f)
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
        unfocusedBorderColor = if (isSystemInDarkTheme()) Color(0xFF4D6654) else MaterialTheme.colorScheme.outline,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
fun FarmFormLabel(text: String) {
    Text(
        text = text,
        color = FarmUi.green,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.titleSmall
    )
}

@Composable
fun FarmLogo(size: androidx.compose.ui.unit.Dp = 56.dp) {
    Surface(
        shape = CircleShape,
        color = if (isSystemInDarkTheme()) Color(0xFF284A39) else Color(0xFFDFF2E4)
    ) {
        Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(R.drawable.ic_farm_wheat),
                contentDescription = "FarmOS wheat logo",
                modifier = Modifier.size(size * 0.68f)
            )
        }
    }
}

@Composable
fun TodayOnFarmCard(
    tasks: List<Task>,
    activeHealthCaseCount: Int,
    birthsDueSoonCount: Int,
    onHealthClick: () -> Unit,
    onBreedingClick: () -> Unit,
    onToggleTask: (Task) -> Unit = {},
    onEditTask: (Task) -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Today on the farm",
            color = FarmUi.green,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )

        if (tasks.isEmpty() && activeHealthCaseCount == 0 && birthsDueSoonCount == 0) {
            Card(
                shape = FarmUi.cardShape,
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No urgent items. You're all caught up.",
                        color = FarmUi.textMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        tasks.forEach { task ->
            Card(
                shape = FarmUi.cardShape,
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onToggleTask(task) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (task.completed) "Mark incomplete" else "Mark complete",
                            tint = if (task.completed) FarmUi.green else FarmUi.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            color = FarmUi.ink,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            val categoryName = when (task.category) {
                                com.samuel.farmos.data.TaskCategory.HEALTH -> "Vaccination"
                                com.samuel.farmos.data.TaskCategory.FEEDING -> "Feeding"
                                com.samuel.farmos.data.TaskCategory.BREEDING -> "Breeding"
                                com.samuel.farmos.data.TaskCategory.MAINTENANCE -> "Maintenance"
                                com.samuel.farmos.data.TaskCategory.OTHER -> "General"
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE55353).copy(alpha = 0.2f),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Text(
                                    text = categoryName,
                                    color = FarmUi.coral,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            val isOverdue = task.dueDateMillis < System.currentTimeMillis() && !task.completed
                            val dateStr = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(task.dueDateMillis))
                            val recurStr = if (task.recurrence != com.samuel.farmos.data.Recurrence.NONE) " • ${task.recurrence.name.lowercase().replaceFirstChar { it.uppercase() }}" else ""
                            Text(
                                text = "• " + (if (isOverdue) "Overdue" else dateStr) + recurStr,
                                color = if (isOverdue) FarmUi.coral else FarmUi.textMuted,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    TextButton(onClick = { onEditTask(task) }) {
                        Text(
                            text = "Edit",
                            color = FarmUi.green,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        if (activeHealthCaseCount > 0) {
            val plural = if (activeHealthCaseCount == 1) "" else "s"
            Card(
                shape = FarmUi.cardShape,
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.coral.copy(alpha = 0.4f)),
                modifier = Modifier.clickable(role = Role.Button, onClick = onHealthClick)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🩺 $activeHealthCaseCount active health case$plural",
                        color = FarmUi.coral,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        if (birthsDueSoonCount > 0) {
            val plural = if (birthsDueSoonCount == 1) "" else "s"
            Card(
                shape = FarmUi.cardShape,
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.purple.copy(alpha = 0.4f)),
                modifier = Modifier.clickable(role = Role.Button, onClick = onBreedingClick)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🍼 $birthsDueSoonCount expected birth$plural in the next 7 days",
                        color = FarmUi.purple,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
