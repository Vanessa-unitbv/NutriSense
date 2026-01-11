package com.example.nutrisense.activities

import android.app.Application
import android.util.Log
import com.example.nutrisense.managers.SharedPreferencesManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ApplicationController : Application() {

    companion object {
        private const val TAG = "ApplicationController"

        lateinit var instance: ApplicationController
            private set
    }

    @Inject
    lateinit var globalPreferencesManager: SharedPreferencesManager

    override fun onCreate() {
        super.onCreate()
        instance = this

        Log.d(TAG, "ApplicationController created")
        initializeApp()
    }

    private fun initializeApp() {
        Log.d(TAG, "Initializing application...")

        try {
            if (globalPreferencesManager.isFirstTimeUser()) {
                Log.d(TAG, "First time user detected, setting up defaults...")
                setupDefaultValues()
                globalPreferencesManager.setFirstTimeUser(false)
            }

            Log.d(TAG, "Application initialization completed")

        } catch (e: Exception) {
            Log.e(TAG, "Error during app initialization: ${e.message}")
        }
    }

    private fun setupDefaultValues() {
        Log.d(TAG, "Setting up default application values...")

        globalPreferencesManager.apply {
            if (getThemeMode() == "system") {
                setThemeMode("system")
            }
        }

        Log.d(TAG, "Default values setup completed")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "ApplicationController terminated")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "Low memory warning received")
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.w(TAG, "Trim memory called with level: $level")
    }
}