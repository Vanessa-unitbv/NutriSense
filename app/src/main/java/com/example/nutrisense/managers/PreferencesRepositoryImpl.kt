package com.example.nutrisense.managers

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val globalPreferencesManager: SharedPreferencesManager
) : PreferencesRepository {

    companion object {
        private const val TAG = "PreferencesRepoImpl"
    }

    override fun getGlobalManager(): SharedPreferencesManager = globalPreferencesManager

    override fun getManagerForUser(userEmail: String?): SharedPreferencesManager {
        if (userEmail == null) return globalPreferencesManager
        return SharedPreferencesManager.getInstance(context, userEmail)
    }

    /**
     * Async version of preferences migration - runs on IO dispatcher to avoid blocking UI
     */
    suspend fun migrateUserPreferencesAsync(userEmail: String): SharedPreferencesManager =
        withContext(Dispatchers.IO) {
            if (userEmail.isBlank()) return@withContext globalPreferencesManager

            try {
                val legacyName = "nutrisense_preferences_${userEmail.replace("@", "_").replace(".", "_")}"
                Log.d(TAG, "Checking legacy prefs: $legacyName for user $userEmail")

                val legacyPrefs = context.getSharedPreferences(legacyName, Context.MODE_PRIVATE)
                val legacyAll = legacyPrefs.all

                if (legacyAll.isNotEmpty()) {
                    val newName = "nutrisense_preferences_${hashEmail(userEmail)}"
                    Log.d(TAG, "Migrating prefs from $legacyName to $newName")

                    val newPrefs = context.getSharedPreferences(newName, Context.MODE_PRIVATE)
                    val newAll = newPrefs.all

                    if (newAll.isEmpty()) {
                        val editor = newPrefs.edit()
                        for ((k, v) in legacyAll) {
                            when (v) {
                                is String -> editor.putString(k, v)
                                is Int -> editor.putInt(k, v)
                                is Boolean -> editor.putBoolean(k, v)
                                is Float -> editor.putFloat(k, v)
                                is Long -> editor.putLong(k, v)
                                is Set<*> -> {
                                    @Suppress("UNCHECKED_CAST")
                                    editor.putStringSet(k, v as Set<String>)
                                }
                                else -> {
                                    // ignore unknown types
                                }
                            }
                        }
                        editor.apply()
                        Log.d(TAG, "Migration applied: ${legacyAll.keys.size} keys moved")
                    } else {
                        Log.d(TAG, "New prefs already contain data, skipping migration")
                    }

                    // Clear legacy to avoid duplication
                    legacyPrefs.edit().clear().apply()
                    Log.d(TAG, "Legacy prefs cleared: $legacyName")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during prefs migration for $userEmail", e)
            }

            SharedPreferencesManager.getInstance(context, userEmail)
        }

    private fun hashEmail(email: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(email.lowercase().toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            email.replace("@", "_").replace(".", "_")
        }
    }
}
