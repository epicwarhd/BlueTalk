package com.bluetalk.android

import android.app.Application
import com.bluetalk.android.ui.theme.ThemePreferenceManager
import com.bluetalk.android.net.ArtiTorManager

/**
 * Main application class for BlueTalk Android
 */
class BlueTalkApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Tor first so any early network goes over Tor
        try {
            val torProvider = ArtiTorManager.getInstance()
            torProvider.init(this)
        } catch (_: Exception){}

        // Initialize favorites persistence early
        try {
            com.bluetalk.android.favorites.FavoritesPersistenceService.initialize(this)
        } catch (_: Exception) { }

        // Initialize theme preference
        ThemePreferenceManager.init(this)

        // Initialize debug preference manager
        try { com.bluetalk.android.ui.debug.DebugPreferenceManager.init(this) } catch (_: Exception) { }

        // Initialize mesh service preferences
        try { com.bluetalk.android.service.MeshServicePreferences.init(this) } catch (_: Exception) { }

        // Proactively start the foreground service to keep mesh alive
        try { com.bluetalk.android.service.MeshForegroundService.start(this) } catch (_: Exception) { }
    }
}
