package com.samuel.farmos.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samuel.farmos.ui.screens.FarmFormLabel
import com.samuel.farmos.ui.screens.FarmUi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** The rounded date control used in the original FarmOS forms. */
@Composable
fun FarmDatePickerField(
    label: String,
    initialMillis: Long?,
    modifier: Modifier = Modifier,
    includeTime: Boolean = false,
    onDateSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    var showPicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val displayText = initialMillis?.let { millis ->
        val pattern = if (includeTime) "d MMM yyyy, h:mm a" else "d MMM yyyy"
        SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
    } ?: "Tap to select"

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FarmFormLabel(label)
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = FarmUi.pillShape,
            border = BorderStroke(1.dp, FarmUi.cardBorderStrong),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmUi.textMuted)
        ) {
            Text(
                displayText,
                color = if (initialMillis == null) FarmUi.green else FarmUi.ink,
                fontWeight = FontWeight.Medium
            )
        }
    }

    LaunchedEffect(showPicker) {
        if (showPicker) {
            val base = Calendar.getInstance().apply { if (initialMillis != null) timeInMillis = initialMillis }
            val dateDialog = DatePickerDialog(
                context,
                { _, year, month, day ->
                    val selected = Calendar.getInstance().apply {
                        if (initialMillis != null) timeInMillis = initialMillis
                        set(Calendar.YEAR, year)
                        set(Calendar.MONTH, month)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onDateSelected(selected.timeInMillis)
                    showPicker = false
                    if (includeTime) showTimePicker = true
                },
                base.get(Calendar.YEAR),
                base.get(Calendar.MONTH),
                base.get(Calendar.DAY_OF_MONTH)
            )
            dateDialog.setOnCancelListener { showPicker = false }
            dateDialog.show()
        }
    }

    LaunchedEffect(showTimePicker) {
        if (showTimePicker) {
            val base = Calendar.getInstance().apply { if (initialMillis != null) timeInMillis = initialMillis }
            val dialog = TimePickerDialog(
                context,
                { _, hour, minute ->
                    val selected = Calendar.getInstance().apply {
                        if (initialMillis != null) timeInMillis = initialMillis
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onDateSelected(selected.timeInMillis)
                    showTimePicker = false
                },
                base.get(Calendar.HOUR_OF_DAY),
                base.get(Calendar.MINUTE),
                false
            )
            dialog.setOnCancelListener { showTimePicker = false }
            dialog.show()
        }
    }
}
