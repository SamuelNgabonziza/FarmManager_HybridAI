package com.samuel.farmos

import android.app.Application
import android.util.Log
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.FirebaseAppCheck

class FarmOsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        installAppCheckProvider()
    }

    private fun installAppCheckProvider() {
        val providerClass = if (BuildConfig.DEBUG) {
            "com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory"
        } else {
            "com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory"
        }
        try {
            val getInstanceMethod = Class.forName(providerClass).getMethod("getInstance")
            val providerFactory = getInstanceMethod.invoke(null) as? AppCheckProviderFactory
            if (providerFactory != null) {
                FirebaseAppCheck.getInstance().installAppCheckProviderFactory(providerFactory)
            }
        } catch (e: Exception) {
            Log.e("FarmOsApplication", "Could not configure Firebase App Check provider", e)
        }
    }
}
