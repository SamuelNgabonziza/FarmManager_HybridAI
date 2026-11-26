package com.samuel.farmos.ui.screens

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.samuel.farmos.ui.theme.FarmOsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    correctPin: String?,
    onSetPin: (String) -> Unit,
    onOpenFinance: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("farm_admin_security", Context.MODE_PRIVATE) }
    val userEmail = remember {
        try { FirebaseAuth.getInstance().currentUser?.email.orEmpty() } catch (_: Exception) { "" }
    }

    var savedQuestion by remember { mutableStateOf(prefs.getString("security_question", "") ?: "") }
    var savedAnswer by remember { mutableStateOf(prefs.getString("security_answer", "") ?: "") }

    var enteredPin by remember { mutableStateOf("") }
    var unlocked by remember { mutableStateOf(correctPin.isNullOrBlank()) }
    var error by remember { mutableStateOf<String?>(null) }

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var questionInput by remember { mutableStateOf(savedQuestion) }
    var answerInput by remember { mutableStateOf("") }
    var mismatchError by remember { mutableStateOf<String?>(null) }

    var isResetMode by remember { mutableStateOf(false) }
    var resetAnswerInput by remember { mutableStateOf("") }
    var resetEmailInput by remember { mutableStateOf("") }
    var resetNewPin by remember { mutableStateOf("") }
    var resetConfirmPin by remember { mutableStateOf("") }
    var resetError by remember { mutableStateOf<String?>(null) }

    var showSecurityUpdate by remember { mutableStateOf(false) }
    var securitySavedMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin & Finance Access", fontWeight = FontWeight.Bold, color = FarmUi.green) },
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
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = FarmUi.surface),
                border = BorderStroke(1.dp, FarmUi.cardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (correctPin.isNullOrBlank()) {
                        Text(
                            "Set an Admin PIN",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.green
                        )
                        OutlinedTextField(
                            value = pin,
                            onValueChange = { pin = it.filter(Char::isDigit).take(4) },
                            placeholder = { Text("New 4-digit PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = { confirmPin = it.filter(Char::isDigit).take(4) },
                            placeholder = { Text("Confirm PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        HorizontalDivider(color = FarmUi.cardBorder)
                        Text(
                            "Security Question (for PIN recovery)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = FarmUi.green,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = questionInput,
                            onValueChange = { questionInput = it },
                            placeholder = { Text("e.g. Name of your first cow?") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        OutlinedTextField(
                            value = answerInput,
                            onValueChange = { answerInput = it },
                            placeholder = { Text("Security answer") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        mismatchError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                val trimmedQ = questionInput.trim()
                                val trimmedA = answerInput.trim().lowercase()
                                when {
                                    pin.length != 4 -> mismatchError = "PIN must be 4 digits"
                                    pin != confirmPin -> mismatchError = "PINs do not match"
                                    trimmedQ.isBlank() || trimmedA.isBlank() ->
                                        mismatchError = "Please set a security question and answer for PIN recovery"
                                    else -> {
                                        mismatchError = null
                                        prefs.edit()
                                            .putString("security_question", trimmedQ)
                                            .putString("security_answer", trimmedA)
                                            .apply()
                                        savedQuestion = trimmedQ
                                        savedAnswer = trimmedA
                                        onSetPin(pin)
                                        unlocked = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = FarmUi.buttonColors()
                        ) {
                            Text("Save PIN", fontWeight = FontWeight.Bold)
                        }
                    } else if (!unlocked && isResetMode) {
                        Text(
                            "Reset Admin PIN",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.green
                        )
                        if (savedQuestion.isNotBlank() && savedAnswer.isNotBlank()) {
                            Text(
                                text = "Security Question: $savedQuestion",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = FarmUi.ink,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = resetAnswerInput,
                                onValueChange = { resetAnswerInput = it },
                                placeholder = { Text("Your security answer") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                        } else {
                            Text(
                                text = "No security question was set yet. Confirm your signed-in account email to reset your PIN:",
                                style = MaterialTheme.typography.bodySmall,
                                color = FarmUi.textMuted,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = resetEmailInput,
                                onValueChange = { resetEmailInput = it },
                                placeholder = { Text("Enter your account email") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                        }
                        OutlinedTextField(
                            value = resetNewPin,
                            onValueChange = { resetNewPin = it.filter(Char::isDigit).take(4) },
                            placeholder = { Text("New 4-digit PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        OutlinedTextField(
                            value = resetConfirmPin,
                            onValueChange = { resetConfirmPin = it.filter(Char::isDigit).take(4) },
                            placeholder = { Text("Confirm new PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        resetError?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                val verified = if (savedQuestion.isNotBlank() && savedAnswer.isNotBlank()) {
                                    resetAnswerInput.trim().equals(savedAnswer, ignoreCase = true)
                                } else {
                                    userEmail.isNotBlank() && resetEmailInput.trim().equals(userEmail, ignoreCase = true)
                                }
                                when {
                                    !verified -> resetError = if (savedQuestion.isNotBlank()) "Incorrect security answer" else "Email does not match your signed-in account"
                                    resetNewPin.length != 4 -> resetError = "PIN must be 4 digits"
                                    resetNewPin != resetConfirmPin -> resetError = "PINs do not match"
                                    else -> {
                                        resetError = null
                                        onSetPin(resetNewPin)
                                        enteredPin = ""
                                        isResetMode = false
                                        unlocked = true
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = FarmUi.buttonColors()
                        ) {
                            Text("Reset & Unlock", fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = {
                            resetError = null
                            isResetMode = false
                        }) {
                            Text("Back to PIN Entry", color = FarmUi.textMuted)
                        }
                    } else if (!unlocked) {
                        Text(
                            "Enter Admin PIN",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = FarmUi.green
                        )
                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = { enteredPin = it.filter(Char::isDigit).take(4) },
                            placeholder = { Text("PIN") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            shape = FarmUi.fieldShape,
                            colors = FarmUi.textFieldColors()
                        )
                        error?.let {
                            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }
                        Button(
                            onClick = {
                                if (enteredPin == correctPin) {
                                    error = null
                                    enteredPin = ""
                                    unlocked = true
                                } else {
                                    error = "Incorrect PIN"
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = FarmUi.buttonColors()
                        ) {
                            Text("Unlock", fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = {
                            error = null
                            resetAnswerInput = ""
                            resetEmailInput = ""
                            resetNewPin = ""
                            resetConfirmPin = ""
                            resetError = null
                            isResetMode = true
                        }) {
                            Text("Forgot PIN?", color = FarmUi.green, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text("Admin area unlocked", style = MaterialTheme.typography.bodyLarge, color = FarmUi.ink)
                        Button(
                            onClick = onOpenFinance,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = FarmUi.buttonColors()
                        ) {
                            Text("Open Finance Records", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showSecurityUpdate = !showSecurityUpdate },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, FarmUi.cardBorder)
                        ) {
                            Text(
                                if (savedQuestion.isBlank()) "Set Security Question" else "Update Security Question",
                                color = FarmUi.green,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (showSecurityUpdate) {
                            OutlinedTextField(
                                value = questionInput,
                                onValueChange = { questionInput = it },
                                placeholder = { Text("e.g. Name of your first cow?") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                            OutlinedTextField(
                                value = answerInput,
                                onValueChange = { answerInput = it },
                                placeholder = { Text("Security answer") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = FarmUi.fieldShape,
                                colors = FarmUi.textFieldColors()
                            )
                            Button(
                                onClick = {
                                    val q = questionInput.trim()
                                    val a = answerInput.trim().lowercase()
                                    if (q.isNotBlank() && a.isNotBlank()) {
                                        prefs.edit()
                                            .putString("security_question", q)
                                            .putString("security_answer", a)
                                            .apply()
                                        savedQuestion = q
                                        savedAnswer = a
                                        answerInput = ""
                                        showSecurityUpdate = false
                                        securitySavedMessage = "Security question saved."
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = FarmUi.buttonColors()
                            ) {
                                Text("Save Security Question", fontWeight = FontWeight.Bold)
                            }
                        }
                        securitySavedMessage?.let {
                            Text(it, color = FarmUi.green, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminScreenPreview() {
    FarmOsTheme { AdminScreen("1234", {}) }
}
