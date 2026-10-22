package com.samuel.farmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.samuel.farmos.ui.components.LocalFarmSnackbar
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentFarmName: String?,
    onUpdateFarmName: (String) -> Unit,
    onNavigateToFarmSwitcher: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    val snackbar = LocalFarmSnackbar.current
    val user = remember {
        try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
    }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings", fontWeight = FontWeight.Bold) }, colors = FarmUi.topAppBarColors())
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Default.AccountCircle,
                    title = "Account & Password",
                    subtitle = user?.email ?: "Review your sign-in account",
                    onClick = { showAccountDialog = true }
                )
            }
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Default.Store,
                    title = "Switch Farm",
                    subtitle = "Manage, view, or add your farms",
                    onClick = onNavigateToFarmSwitcher
                )
            }
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "Admin / Finance",
                    subtitle = "PIN-protected farm records and settings",
                    onClick = onNavigateToAdmin
                )
            }

            Button(
                onClick = { showSignOutConfirm = true },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = FarmUi.secondaryButtonColors()
            ) {
                Icon(Icons.Default.Logout, contentDescription = null)
                Spacer(Modifier.height(0.dp).padding(horizontal = 4.dp))
                Text("Log Out")
            }
        }
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = FarmUi.green) },
            title = { Text("Account & Password", color = FarmUi.green, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Registered Email:", color = FarmUi.green, fontWeight = FontWeight.SemiBold)
                    Text(user?.email ?: "No email address on this account", color = FarmUi.ink)
                    Text("Verify your password", color = FarmUi.green, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isVerifying && !password.isBlank(),
                    onClick = {
                        val email = user?.email
                        if (email == null) {
                            snackbar.show("This account does not have an email password to verify.")
                        } else {
                            isVerifying = true
                            user.reauthenticate(EmailAuthProvider.getCredential(email, password))
                                .addOnCompleteListener { task ->
                                    isVerifying = false
                                    snackbar.show(if (task.isSuccessful) "Account verified." else task.exception?.localizedMessage ?: "Password could not be verified.")
                                    if (task.isSuccessful) showAccountDialog = false
                                }
                        }
                    }
                ) { Text(if (isVerifying) "Verifying…" else "Verify") }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        val email = user?.email
                        if (email.isNullOrBlank()) snackbar.show("No email address is available for a password reset.")
                        else FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task -> snackbar.show(if (task.isSuccessful) "Password reset email sent." else task.exception?.localizedMessage ?: "Could not send reset email.") }
                    }) { Text("Reset via Email") }
                    TextButton(onClick = { showAccountDialog = false }) { Text("Close") }
                }
            },
            containerColor = FarmUi.surface
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("You will need to sign in again to access your farm data on this device.") },
            confirmButton = {
                TextButton(onClick = { showSignOutConfirm = false; onSignOut() }) {
                    Text("Log Out", color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showSignOutConfirm = false }) { Text("Cancel") } },
            containerColor = FarmUi.surface
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FarmUi.cardShape,
        colors = CardDefaults.cardColors(containerColor = FarmUi.surface)
    ) { Column(content = { content() }) }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    androidx.compose.material3.TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 3.dp)) {
        Icon(icon, contentDescription = null, tint = FarmUi.green)
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, color = FarmUi.green, fontWeight = FontWeight.Bold)
            Text(subtitle, color = FarmUi.textMuted, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = FarmUi.textMuted)
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FarmOsTheme {
        SettingsScreen("Green Valley", {}, {}, {}, {})
    }
}
