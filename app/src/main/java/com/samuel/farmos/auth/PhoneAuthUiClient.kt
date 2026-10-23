package com.samuel.farmos.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

/** Starts Firebase's SMS verification flow and creates credentials from entered codes. */
class PhoneAuthUiClient(
    private val context: Context,
    private val auth: FirebaseAuth?
) {
    fun sendVerificationCode(
        phoneNumber: String,
        onCodeSent: (String) -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (auth == null) {
            onFailure("Firebase is not initialized.")
            return
        }
        val activity = findActivity(context)
        if (activity == null) {
            onFailure("Phone verification requires an active app window.")
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                onVerificationCompleted.invoke(credential)
            }

            override fun onVerificationFailed(error: FirebaseException) {
                onFailure(error.localizedMessage ?: "Could not verify this phone number.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun credentialFromCode(verificationId: String, smsCode: String): PhoneAuthCredential =
        PhoneAuthProvider.getCredential(verificationId, smsCode)

    private fun findActivity(start: Context): Activity? {
        var current: Context? = start
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}
