package de.schmidt.bucket.utils

import android.content.Context
import android.content.res.Configuration

object ThemeUtils {
    fun isInDarkMode(context: Context): Boolean {
        when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> {
                return false
            } // Night mode is not active, we're using the light theme
            Configuration.UI_MODE_NIGHT_YES -> {
                return true
            } // Night mode is active, we're using dark theme
        }

        return false
    }
}