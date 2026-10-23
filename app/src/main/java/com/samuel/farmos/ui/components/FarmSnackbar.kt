package com.samuel.farmos.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * The app-wide snackbar.
 *
 * Android guidance is to use a `Snackbar` rather than a `Toast` for messages
 * that belong to the app: a snackbar is anchored to the current screen, can
 * carry an action, and does not float over the system status bar the way a
 * toast does.
 *
 * This is exposed as a composition local so a screen can raise a message
 * without having to thread a `SnackbarHostState` through every callback:
 *
 * ```
 * val snackbar = LocalFarmSnackbar.current
 * snackbar.show("Farm name updated")
 * ```
 */
class FarmSnackbar internal constructor(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope
) {
    /** Shows a short message. */
    fun show(message: String) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            hostState.showSnackbar(message)
        }
    }

    /**
     * Shows a message with an action, for example "Deleted — Undo". Returns
     * the label the user picked, or null if the snackbar was dismissed.
     */
    fun showActionable(
        message: String,
        actionLabel: String,
        onAction: () -> Unit
    ) {
        scope.launch {
            hostState.currentSnackbarData?.dismiss()
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
            if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                onAction()
            }
        }
    }
}

/**
 * The current [FarmSnackbar]. Screens that are always hosted inside
 * [FarmSnackbarProvider] can use it without any extra plumbing.
 */
val LocalFarmSnackbar = staticCompositionLocalOf<FarmSnackbar> {
    error("No FarmSnackbar provided. Wrap the content in FarmSnackbarProvider.")
}

/**
 * Hosts the single app-level snackbar and publishes it to descendants.
 *
 * The host is placed once, at the root of the navigation graph, so a message
 * raised on one screen is visible there rather than only on the screen that
 * raised it.
 */
@Composable
fun FarmSnackbarProvider(
    hostState: SnackbarHostState,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    CompositionLocalProvider(
        LocalFarmSnackbar provides FarmSnackbar(hostState, scope),
        content = content
    )
}
