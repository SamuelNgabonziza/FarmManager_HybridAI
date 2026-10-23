package com.samuel.farmos.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * A delete affordance that always asks for confirmation first.
 *
 * Android's guidance is that any action that destroys data the user cannot
 * get back must be confirmed, so the confirmation is built into the button
 * rather than being left to each screen to remember.
 *
 * @param description what will be deleted, shown in the dialog. Naming the
 *   record is what makes the dialog meaningful to the user.
 */
@Composable
fun FarmDeleteButton(
    description: String,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showConfirm = true },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Delete $description",
            tint = MaterialTheme.colorScheme.error
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Delete?") },
            text = {
                Text(
                    if (description.isNotBlank())
                        "$description will be permanently removed. This cannot be undone."
                    else
                        "This item will be permanently removed. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}
