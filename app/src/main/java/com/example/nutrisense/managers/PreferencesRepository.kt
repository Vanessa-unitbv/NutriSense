@file:Suppress("unused")

package com.example.nutrisense.managers

interface PreferencesRepository {
    fun getGlobalManager(): SharedPreferencesManager
    fun getManagerForUser(userEmail: String?): SharedPreferencesManager
}
