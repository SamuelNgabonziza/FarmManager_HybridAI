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
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.samuel.farmos.auth.GoogleAuthUiClient
import com.samuel.farmos.auth.PhoneAuthUiClient
import com.google.firebase.auth.PhoneAuthCredential
import androidx.compose.material3.SnackbarHostState
import com.samuel.farmos.ui.components.FarmSnackbarProvider
import com.samuel.farmos.ui.components.GoogleSignInButton
import com.samuel.farmos.ui.components.LocalFarmSnackbar
import com.samuel.farmos.ui.theme.FarmOsTheme
import androidx.compose.ui.tooling.preview.Preview
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
    val phoneAuthClient = remember { PhoneAuthUiClient(context, auth) }

    var farmName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var accountCreated by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isFinishingPhoneLink by remember { mutableStateOf(false) }

    fun finishPhoneLink(credential: PhoneAuthCredential) {
        if (isFinishingPhoneLink) return
        if (auth == null) {
            snackbar.show("Firebase is not initialized.")
            return
        }
        val user = auth.currentUser
        if (user == null) {
            snackbar.show("Create your account before verifying your phone number.")
            return
        }
        isFinishingPhoneLink = true
        user.linkWithCredential(credential).addOnCompleteListener { task ->
            isFinishingPhoneLink = false
            if (task.isSuccessful) {
                val uid = task.result?.user?.uid ?: auth.currentUser?.uid
                if (uid != null) onAuthSuccess(uid, farmName.ifBlank { null })
            } else {
                snackbar.show(task.exception?.localizedMessage ?: "Could not link this phone number.")
            }
        }
    }

    fun sendPhoneCode() {
        val enteredPhone = phoneNumber.trim()
        if (!enteredPhone.startsWith("+")) {
            snackbar.show("Enter your phone number with its country code, for example +256...")
            return
        }
        isLoading = true
        phoneAuthClient.sendVerificationCode(
            phoneNumber = enteredPhone,
            onCodeSent = { id ->
                verificationId = id
                isLoading = false
                snackbar.show("Verification code sent by SMS.")
            },
            onVerificationCompleted = { credential ->
                isLoading = false
                finishPhoneLink(credential)
            },
            onFailure = { message ->
                isLoading = false
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
                text = "Create Your Farm Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = FarmUi.deepGreen
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Set up your farm and start tracking everything in one place",
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
                        value = farmName,
                        onValueChange = { farmName = it },
                        label = { Text("Farm Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Phone Number (with country code)") },
                        placeholder = { Text("+256...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !accountCreated,
                        shape = FarmUi.fieldShape,
                        colors = FarmUi.textFieldColors()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !accountCreated,
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
                        enabled = !accountCreated,
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

                    if (accountCreated && verificationId != null) {
                        OutlinedTextField(
                            value = smsCode,
                            onValueChange = { smsCode = it.filter(Char::isDigit).take(6) },
                            label = { Text("SMS verification code") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            if (accountCreated) {
                                val id = verificationId ?: return@Button
                                if (smsCode.length != 6) {
                                    snackbar.show("Enter the 6-digit code from the SMS.")
                                    return@Button
                                }
                                finishPhoneLink(phoneAuthClient.credentialFromCode(id, smsCode))
                                return@Button
                            }
                            if (email.isBlank() || password.isBlank() || phoneNumber.isBlank()) return@Button
                            if (auth == null) {
                                snackbar.show("Firebase is not initialized.")
                                return@Button
                            }
                            isLoading = true
                            auth.createUserWithEmailAndPassword(email.trim(), password)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        accountCreated = true
                                        sendPhoneCode()
                                    } else {
                                        snackbar.show(
                                            task.exception?.localizedMessage ?: "Sign up failed"
                                        )
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = if (accountCreated) {
                            !isLoading && !isFinishingPhoneLink && smsCode.length == 6
                        } else {
                            !isLoading && email.isNotBlank() && password.isNotBlank() && phoneNumber.isNotBlank()
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = FarmUi.buttonColors()
                    ) {
                        Text(if (accountCreated) "Verify Phone & Finish" else "Create Account & Send Code")
                    }

                    if (accountCreated) {
                        TextButton(
                            onClick = { sendPhoneCode() },
                            enabled = !isLoading && !isFinishingPhoneLink,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Resend SMS code") }
                    } else {
                        Text(
                            "We’ll text a code to verify your phone. Standard SMS rates may apply.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmUi.textMuted
                        )
                    }
                }
            }

            if (!accountCreated) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                GoogleSignInButton(
                    onClick = {
                        if (isLoading) return@GoogleSignInButton
                        isLoading = true
                        scope.launch {
                            val uid = googleAuthClient.signIn()
                            isLoading = false
                            if (uid != null) onAuthSuccess(uid, farmName.ifBlank { null })
                            else snackbar.show(googleAuthClient.lastErrorMessage ?: "Sign in failed")
                        }
                    },
                    enabled = !isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    color = FarmUi.textMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In")
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
fun AuthScreenPreview() {
    FarmOsTheme {
        FarmSnackbarProvider(hostState = remember { SnackbarHostState() }) {
            AuthScreen(
                onSkip = {},
                onAuthSuccess = { _, _ -> },
                onNavigateToLogin = {}
            )
        }
    }
}
