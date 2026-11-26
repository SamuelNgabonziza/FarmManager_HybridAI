package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.samuel.farmos.auth.GoogleAuthUiClient
import com.samuel.farmos.ui.components.FarmSnackbarProvider
import com.samuel.farmos.ui.components.GoogleSignInButton
import com.samuel.farmos.ui.components.LocalFarmSnackbar
import com.samuel.farmos.ui.theme.FarmOsTheme
import androidx.compose.foundation.layout.statusBarsPadding
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onSkip: () -> Unit,
    onAuthSuccess: (uid: String, initialFarmName: String?) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = LocalFarmSnackbar.current
    val auth = remember {
        try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }
    }
    val googleAuthClient = remember { GoogleAuthUiClient(context, auth) }

    var farmName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FarmUi.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FarmLogo(56.dp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Create Your Farm Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = FarmUi.green
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Set up your farm and start tracking everything in one place",
                style = MaterialTheme.typography.bodyMedium,
                color = FarmUi.textMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    FarmFormLabel("Farm Name (Optional)")
                    OutlinedTextField(
                        value = farmName,
                        onValueChange = { farmName = it },
                        placeholder = { Text("e.g. Green Valley Farm") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FarmFormLabel("Email Address")
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("you@example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FarmFormLabel("Password")
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Create a password (min 6 characters)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors(),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                    tint = FarmUi.textMuted
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val trimmedEmail = email.trim()
                            if (trimmedEmail.isBlank() || password.isBlank()) {
                                snackbar.show("Please enter your email and password.")
                                return@Button
                            }
                            if (password.length < 6) {
                                snackbar.show("Password must be at least 6 characters.")
                                return@Button
                            }
                            if (auth == null) {
                                snackbar.show("Firebase Auth is unavailable.")
                                return@Button
                            }
                            isLoading = true
                            auth.createUserWithEmailAndPassword(trimmedEmail, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        task.result?.user?.uid?.let { onAuthSuccess(it, farmName.trim().ifBlank { null }) }
                                    } else {
                                        snackbar.show(task.exception?.localizedMessage ?: "Failed to create account.")
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(20.dp),
                        colors = FarmUi.buttonColors()
                    ) {
                        Text(if (isLoading) "Creating Account…" else "Sign Up", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = FarmUi.cardBorder)
                        Text(
                            text = "  OR  ",
                            color = FarmUi.textMuted,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = FarmUi.cardBorder)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    GoogleSignInButton(
                        text = "Continue with Google",
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val uid = googleAuthClient.signIn()
                                isLoading = false
                                if (uid != null) {
                                    onAuthSuccess(uid, farmName.trim().ifBlank { null })
                                } else {
                                    val err = googleAuthClient.lastErrorMessage
                                    if (err != null && !err.contains("cancell", ignoreCase = true)) {
                                        snackbar.show(err)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account?", color = FarmUi.textMuted, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Log In", color = FarmUi.green, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSkip) {
                Text("Skip for now", color = FarmUi.textMuted, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    FarmOsTheme {
        FarmSnackbarProvider(hostState = remember { SnackbarHostState() }) {
            AuthScreen(onSkip = {}, onAuthSuccess = { _, _ -> }, onNavigateToLogin = {})
        }
    }
}
