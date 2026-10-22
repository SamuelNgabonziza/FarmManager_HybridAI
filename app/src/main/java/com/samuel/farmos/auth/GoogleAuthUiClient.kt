package com.samuel.farmos.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.samuel.farmos.BuildConfig
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthUiClient(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth?
) {
    var lastErrorMessage: String? = null
        private set

    private fun findActivity(context: Context): Activity? {
        var current: Context? = context
        while (current is ContextWrapper) {
            if (current is Activity) {
                return current
            }
            current = current.baseContext
        }
        return null
    }

    suspend fun signIn(): String? {
        lastErrorMessage = null
        if (firebaseAuth == null) {
            lastErrorMessage = "Firebase is not initialized."
            Log.e("GoogleAuth", lastErrorMessage!!)
            return null
        }
        val activity = findActivity(context) ?: run {
            lastErrorMessage = "Google Sign-In requires an active Activity window."
            Log.e("GoogleAuth", lastErrorMessage!!)
            return null
        }

        return try {
            Log.d("GoogleAuth", "Initializing CredentialManager...")
            val credentialManager = CredentialManager.create(activity)
            Log.d("GoogleAuth", "Using Web Client ID: ${BuildConfig.GOOGLE_WEB_CLIENT_ID}")

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setNonce(generateNonce())
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .build()

            Log.d("GoogleAuth", "Requesting credential from CredentialManager...")
            val result = credentialManager.getCredential(activity, request)
            Log.d("GoogleAuth", "Credential retrieved successfully.")

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d("GoogleAuth", "Authenticating with Firebase...")
                val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()
                val uid = authResult.user?.uid
                Log.d("GoogleAuth", "Firebase authentication successful for UID: $uid")
                uid
            } else {
                Log.e("GoogleAuth", "Unexpected credential type: ${credential.type}")
                lastErrorMessage = "Unexpected credential type received from Google."
                null
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d("GoogleAuth", "Sign in cancelled by user")
            lastErrorMessage = "Sign in was cancelled."
            null
        } catch (e: NoCredentialException) {
            Log.e("GoogleAuth", "No credentials available", e)
            lastErrorMessage = "No Google accounts found on this device. Please add a Google account in your phone's Settings first."
            null
        } catch (e: Throwable) {
            Log.e("GoogleAuth", "Google sign in failed with exception", e)
            val msg = e.localizedMessage ?: e.message ?: "Authentication error"
            lastErrorMessage = when {
                msg.contains("10", ignoreCase = true) || msg.contains("Developer", ignoreCase = true) ->
                    "Google Sign-In config error. Please verify SHA-1 is registered in Firebase Console."
                msg.contains("network", ignoreCase = true) ->
                    "Network error. Please check your internet connection and try again."
                msg.contains("canceled", ignoreCase = true) || msg.contains("cancelled", ignoreCase = true) ->
                    "Sign in was cancelled."
                else -> "Google Sign-In failed: $msg"
            }
            null
        }
    }

    private fun generateNonce(): String {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
