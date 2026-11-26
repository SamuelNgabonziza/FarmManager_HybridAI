package com.samuel.farmos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.samuel.farmos.auth.GoogleAuthUiClient
import com.samuel.farmos.ui.components.FarmSnackbarProvider
import com.samuel.farmos.ui.components.GoogleSignInButton
import com.samuel.farmos.ui.components.LocalFarmSnackbar
import com.samuel.farmos.ui.theme.FarmOsTheme
import androidx.compose.foundation.layout.statusBarsPadding
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onSkip: () -> Unit,
    onAuthSuccess: (uid: String) -> Unit,
    onNavigateToSignUp: () -> Unit
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
            Surface(
                modifier = Modifier.size(60.dp),
                shape = CircleShape,
                color = Color(0xFF80E3A7).copy(alpha = 0.85f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🚜", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = FarmUi.green
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Log in to manage your livestock, tasks, and farm data",
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
                        placeholder = { Text("Enter your password") },
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = FarmUi.green,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                val trimmed = email.trim()
                                if (trimmed.isBlank()) {
                                    snackbar.show("Enter your email above to reset password.")
                                } else if (auth == null) {
                                    snackbar.show("Authentication is unavailable.")
                                } else {
                                    auth.sendPasswordResetEmail(trimmed).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            snackbar.show("Reset link sent to $trimmed")
                                        } else {
                                            snackbar.show(task.exception?.localizedMessage ?: "Failed to send reset link.")
                                        }
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val trimmedEmail = email.trim()
                            if (trimmedEmail.isBlank() || password.isBlank()) {
                                snackbar.show("Please enter your email and password.")
                                return@Button
                            }
                            if (auth == null) {
                                snackbar.show("Firebase Auth is unavailable.")
                                return@Button
                            }
                            isLoading = true
                            auth.signInWithEmailAndPassword(trimmedEmail, password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        task.result?.user?.uid?.let { onAuthSuccess(it) }
                                    } else {
                                        snackbar.show(task.exception?.localizedMessage ?: "Failed to sign in.")
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
                        Text(if (isLoading) "Signing In…" else "Log In →", fontWeight = FontWeight.Bold)
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
                                    onAuthSuccess(uid)
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
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSignUp() },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account? ", color = FarmUi.green, style = MaterialTheme.typography.bodyMedium)
                Text("Sign Up", color = FarmUi.green, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Skip for now",
                color = FarmUi.textMuted,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onSkip() }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    FarmOsTheme {
        FarmSnackbarProvider(hostState = remember { SnackbarHostState() }) {
            LoginScreen(onSkip = {}, onAuthSuccess = {}, onNavigateToSignUp = {})
        }
    }
}
