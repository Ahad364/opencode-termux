package com.dualmode.keyboard.ime

import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.dualmode.keyboard.data.KeyboardSettings
import com.dualmode.keyboard.data.SettingsDefaults
import com.dualmode.keyboard.keyboard.KeyboardLayouts
import com.dualmode.keyboard.keyboard.KeyboardView
import com.dualmode.keyboard.utils.dp

/**
 * Owns the keyboard view hierarchy inside the IME window and re-renders it on
 * state changes (including the sticky Ctrl active-state feedback).
 */
class KeyboardController(
    private val context: android.content.Context,
    val state: KeyboardState,
    private val viewListener: KeyboardView.Listener
) {

    var settings: KeyboardSettings = SettingsDefaults.SETTINGS
        set(value) {
            field = value
            currentView?.let { configure(it) }
        }

    private var container: FrameLayout? = null
    private var currentView: KeyboardView? = null

    /** Builds and returns the root container with the keyboard shown. */
    fun buildRoot(): View {
        val frame = FrameLayout(context)
        container = frame
        show(animate = false)
        return frame
    }

    fun show(animate: Boolean) {
        val frame = container ?: return
        frame.removeAllViews()
        val view = KeyboardView(context)
        configure(view)
        frame.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        if (animate && settings.animationsEnabled) {
            view.translationY = 80f.dp(context)
            view.animate()
                .translationY(0f)
                .setDuration(170)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
        currentView = view
    }

    /** Re-renders the current view from state (called on every state change). */
    fun refresh() {
        currentView?.let { configure(it) }
    }

    private fun configure(view: KeyboardView) {
        view.applySettings(settings)
        view.popupsEnabled = !state.disablePopups
        view.ctrlActive = state.ctrlActive
        view.altActive = state.altActive
        view.fnActive = state.fnActive
        view.listener = viewListener
        view.rows = KeyboardLayouts.forState(
            page = state.currentPage(),
            language = state.language,
            shifted = state.isShifted,
            ctrlActive = state.ctrlActive
        )
    }
}
