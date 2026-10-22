package com.samuel.farmos.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.samuel.farmos.auth.PhoneAuthUiClient
import com.google.firebase.auth.PhoneAuthCredential
import com.samuel.farmos.ui.components.FarmSnackbarProvider
import com.samuel.farmos.ui.components.GoogleSignInButton
import com.samuel.farmos.ui.components.LocalFarmSnackbar
import com.samuel.farmos.ui.theme.FarmOsTheme
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
    val phoneAuthClient = remember { PhoneAuthUiClient(context, auth) }

    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isPhoneLoading by remember { mutableStateOf(false) }
    var isCompletingPhoneSignIn by remember { mutableStateOf(false) }

    fun finishPhoneSignIn(credential: PhoneAuthCredential) {
        if (isCompletingPhoneSignIn || auth == null) return
        isCompletingPhoneSignIn = true
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            isCompletingPhoneSignIn = false
            if (task.isSuccessful) {
                val uid = task.result?.user?.uid ?: auth.currentUser?.uid
                if (uid != null) onAuthSuccess(uid)
            } else {
                snackbar.show(task.exception?.localizedMessage ?: "Phone sign-in failed.")
            }
        }
    }

    fun sendPhoneCode() {
        val enteredPhone = phoneNumber.trim()
        if (!enteredPhone.startsWith("+")) {
            snackbar.show("Enter your phone number with its country code, for example +256...")
            return
        }
        isPhoneLoading = true
        phoneAuthClient.sendVerificationCode(
            phoneNumber = enteredPhone,
            onCodeSent = { id ->
                verificationId = id
                isPhoneLoading = false
                snackbar.show("Verification code sent by SMS.")
            },
            onVerificationCompleted = { credential ->
                isPhoneLoading = false
                finishPhoneSignIn(credential)
            },
            onFailure = { message ->
                isPhoneLoading = false
                snackbar.show(message)
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = FarmUi.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FarmLogo(64.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = FarmUi.deepGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Log in to manage your livestock, tasks, and farm data",
                style = MaterialTheme.typography.bodyMedium,
                color = FarmUi.textMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors(),
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) return@Button
                            if (auth == null) {
                                snackbar.show("Firebase is not initialized.")
                                return@Button
                            }
                            isLoading = true
                            auth.signInWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        val uid = auth.currentUser?.uid
                                        if (uid != null) {
                                            onAuthSuccess(uid)
                                        }
                                    } else {
                                        snackbar.show(task.exception?.localizedMessage ?: "Login failed")
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = FarmUi.buttonColors()
                    ) {
                        Text("Sign In")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    GoogleSignInButton(
                        onClick = {
                            if (isLoading) return@GoogleSignInButton
                            isLoading = true
                            scope.launch {
                                val uid = googleAuthClient.signIn()
                                isLoading = false
                                if (uid != null) onAuthSuccess(uid)
                                else snackbar.show(googleAuthClient.lastErrorMessage ?: "Sign in failed")
                            }
                        },
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Sign in with your phone",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FarmUi.deepGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (with country code)") },
                        placeholder = { Text("+256...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isPhoneLoading && !isCompletingPhoneSignIn,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )
                    Text(
                        "We’ll text a verification code. Standard SMS rates may apply.",
                        style = MaterialTheme.typography.bodySmall,
                        color = FarmUi.textMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { sendPhoneCode() },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        enabled = !isPhoneLoading && !isCompletingPhoneSignIn && phoneNumber.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = FarmUi.buttonColors()
                    ) {
                        Text(if (verificationId == null) "Send SMS Code" else "Resend SMS Code")
                    }

                    if (verificationId != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = smsCode,
                            onValueChange = { smsCode = it.filter(Char::isDigit).take(6) },
                            label = { Text("SMS verification code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val id = verificationId ?: return@Button
                                if (smsCode.length != 6) {
                                    snackbar.show("Enter the 6-digit code from the SMS.")
                                } else {
                                    finishPhoneSignIn(phoneAuthClient.credentialFromCode(id, smsCode))
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            enabled = !isCompletingPhoneSignIn && !isPhoneLoading && smsCode.length == 6,
                            shape = RoundedCornerShape(14.dp),
                            colors = FarmUi.buttonColors()
                        ) {
                            Text("Verify & Sign In")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    color = FarmUi.textMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToSignUp) {
                    Text("Sign Up")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onSkip) {
                Text("Skip for now", color = FarmUi.textMuted)
            }
        }
    }
}
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    FarmOsTheme {
        FarmSnackbarProvider(hostState = remember { SnackbarHostState() }) {
            LoginScreen(
                onSkip = {},
                onAuthSuccess = {},
                onNavigateToSignUp = {}
            )
        }
    }
}

