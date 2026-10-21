package com.samuel.farmos.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AuthGateDialog(
    onDismiss: () -> Unit,
    onSignUp: () -> Unit,
    onLogin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Account Required")
        },
        text = {
            Text(text = "Sign in or create an account to manage farm records, animals, tasks, and use AI features.")
        },
        confirmButton = {
            Button(
                onClick = onSignUp,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(text = "Create Account")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onLogin) {
                    Text(text = "Sign In")
                }
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onDismiss) {
                    Text(text = "Cancel")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
