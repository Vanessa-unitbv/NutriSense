package com.example.nutrisense.di

import android.content.Context
import android.util.Log
import com.example.nutrisense.managers.PreferencesRepository
import com.example.nutrisense.managers.PreferencesRepositoryImpl
import com.example.nutrisense.managers.SharedPreferencesManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    private const val TAG = "PreferencesModule"

    @Provides
    @Singleton
    fun provideSharedPreferencesManager(
        @ApplicationContext context: Context
    ): SharedPreferencesManager {
        val manager = SharedPreferencesManager.getGlobalInstance(context)
        val email = manager.getUserEmail()
        Log.d(TAG, "Restored current user email from global prefs: $email")
        SharedPreferencesManager.setCurrentUser(email)
        return manager
    }

    @Provides
    @Singleton
    fun providePreferencesRepository(
        @ApplicationContext context: Context,
        sharedPreferencesManager: SharedPreferencesManager
    ): PreferencesRepository {
        return PreferencesRepositoryImpl(context, sharedPreferencesManager)
    }
}