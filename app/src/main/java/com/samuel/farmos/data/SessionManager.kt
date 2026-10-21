package com.samuel.farmos.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persists the small amount of session state that has to survive process death:
 * whether the user is signed in, and which farm is currently selected.
 *
 * Backed by [SharedPreferences] because the payload is two primitives; DataStore
 * would add a dependency without buying anything at this size.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isLoggedIn = MutableStateFlow(prefs.getBoolean(KEY_IS_LOGGED_IN, false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _activeFarmId = MutableStateFlow(prefs.getString(KEY_ACTIVE_FARM_ID, null))
    val activeFarmId: StateFlow<String?> = _activeFarmId.asStateFlow()

    fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()
        _isLoggedIn.value = value
    }

    fun completeLogin() {
        setLoggedIn(true)
    }

    fun setActiveFarmId(farmId: String?) {
        val editor = prefs.edit()
        if (farmId == null) {
            editor.remove(KEY_ACTIVE_FARM_ID)
        } else {
            editor.putString(KEY_ACTIVE_FARM_ID, farmId)
        }
        editor.apply()
        _activeFarmId.value = farmId
    }

    /** Called on sign-out: drop everything tied to the previous account. */
    fun clear() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_ACTIVE_FARM_ID)
            .apply()
        _isLoggedIn.value = false
        _activeFarmId.value = null
    }

    companion object {
        private const val PREFS_NAME = "farmos_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_ACTIVE_FARM_ID = "active_farm_id"
    }
}
