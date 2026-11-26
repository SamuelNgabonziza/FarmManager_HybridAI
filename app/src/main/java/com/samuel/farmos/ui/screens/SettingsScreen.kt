package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.samuel.farmos.ui.components.FarmSnackbarProvider
import com.samuel.farmos.ui.components.LocalFarmSnackbar
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentFarmName: String?,
    onUpdateFarmName: (String) -> Unit,
    onNavigateToFarmSwitcher: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit = {}
) {
    val snackbar = LocalFarmSnackbar.current
    val user = remember {
        try { FirebaseAuth.getInstance().currentUser } catch (_: Exception) { null }
    }

    var showAccountDialog by remember { mutableStateOf(false) }

    var showSignOutConfirm by remember { mutableStateOf(false) }
    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = FarmUi.green) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FarmUi.green)
                    }
                },
                colors = FarmUi.topAppBarColors()
            )
        },
        containerColor = FarmUi.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsGroup {
                SettingsRow(
                    icon = Icons.Default.AccountCircle,
                    title = "Account & Password",
                    subtitle = user?.email ?: "Review your sign-in account",
                    iconBackground = FarmUi.settingsIconMint,
                    iconTint = FarmUi.green,
                    onClick = {
                        password = ""
                        isPasswordVisible = false
                        showAccountDialog = true
                    }
                )
            }

            Text(
                "Farm Management",
                style = MaterialTheme.typography.titleMedium,
                color = FarmUi.ink,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            SettingsGroup {
                SettingsRow(
                    icon = Icons.Default.Store,
                    title = "Switch Farm",
                    subtitle = "Manage, view, or add your farms",
                    iconBackground = FarmUi.settingsIconMint,
                    iconTint = FarmUi.green,
                    onClick = onNavigateToFarmSwitcher
                )
            }

            SettingsGroup {
                SettingsRow(
                    icon = Icons.Default.Lock,
                    title = "Admin / Finance",
                    subtitle = "PIN-protected farm records and settings",
                    iconBackground = FarmUi.settingsIconWarm,
                    iconTint = FarmUi.orange,
                    onClick = onNavigateToAdmin
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedButton(
                onClick = { showSignOutConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = FarmUi.coral),
                border = BorderStroke(1.dp, FarmUi.coral.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = FarmUi.coral)
                Spacer(Modifier.size(8.dp))
                Text("Log Out", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = {
                password = ""
                isPasswordVisible = false
                showAccountDialog = false
            },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = FarmUi.green, modifier = Modifier.size(32.dp)) },
            title = { Text("Account & Password", color = FarmUi.green, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Registered Email:", color = FarmUi.green, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text(user?.email ?: "No email address on this account", color = FarmUi.ink, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("Verify your password", color = FarmUi.green, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter your password") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    tint = FarmUi.textMuted
                                )
                            }
                        },
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = !isVerifying && password.isNotBlank(),
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
                                    if (task.isSuccessful) {
                                        password = ""
                                        isPasswordVisible = false
                                        showAccountDialog = false
                                    }
                                }
                        }
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = FarmUi.buttonColors()
                ) {
                    Text(if (isVerifying) "Verifying…" else "Verify", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        val email = user?.email
                        if (email.isNullOrBlank()) snackbar.show("No email address is available for a password reset.")
                        else FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                snackbar.show(if (task.isSuccessful) "Password reset email sent." else task.exception?.localizedMessage ?: "Could not send reset email.")
                            }
                    }) {
                        Text("Reset via Email", color = FarmUi.textMuted)
                    }
                    TextButton(onClick = {
                        password = ""
                        isPasswordVisible = false
                        showAccountDialog = false
                    }) {
                        Text("Close", color = FarmUi.textMuted)
                    }
                }
            },
            containerColor = FarmUi.dialogSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Log out?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
            text = { Text("You will need to sign in again to access your farm data on this device.", color = FarmUi.ink) },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        onSignOut()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = Color.White
                    )
                ) {
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel", color = FarmUi.textMuted)
                }
            },
            containerColor = FarmUi.dialogSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FarmUi.cardShape,
        colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
        border = BorderStroke(1.dp, FarmUi.cardBorder)
    ) {
        Column(content = { content() })
    }
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconBackground: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(14.dp), color = iconBackground) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
        }
        Column(modifier = Modifier.weight(1f).padding(start = 14.dp)) {
            Text(title, color = FarmUi.ink, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(
                text = subtitle,
                color = FarmUi.textMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    FarmOsTheme {
        FarmSnackbarProvider(hostState = remember { SnackbarHostState() }) {
            SettingsScreen("Green Valley", {}, {}, {}, {})
        }
    }
}
