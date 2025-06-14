package com.example.nutrisense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.data.database.AppDatabase
import com.example.nutrisense.data.entity.User
import com.example.nutrisense.data.repository.UserRepository
import com.example.nutrisense.data.preferences.SharedPreferencesManager
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    private val preferencesManager: SharedPreferencesManager

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
        repository = UserRepository(userDao)
        preferencesManager = SharedPreferencesManager.getInstance(application)
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onError("Email and password are required")
            return
        }

        viewModelScope.launch {
            try {
                val user = repository.loginUser(email, password)
                if (user != null) {
                    preferencesManager.setUserLoggedIn(user.email, user.firstName)

                    if (preferencesManager.isFirstTimeUser()) {
                        setDefaultNutritionGoals()
                        preferencesManager.setFirstTimeUser(false)
                    }

                    onSuccess(user)
                } else {
                    onError("Invalid email or password")
                }
            } catch (e: Exception) {
                onError("Login error: ${e.message}")
            }
        }
    }

    fun registerUser(
        email: String,
        password: String,
        firstName: String?,
        lastName: String?,
        age: Int?,
        onSuccess: (User) -> Unit,
        onError: (String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank()) {
            onError("Email and password are required")
            return
        }

        viewModelScope.launch {
            try {
                if (repository.isEmailExists(email)) {
                    onError("Email is already registered")
                    return@launch
                }

                val user = User(
                    email = email,
                    password = password,
                    firstName = firstName,
                    lastName = lastName,
                    age = age
                )

                val userId = repository.registerUser(user)
                val newUser = user.copy(id = userId)

                preferencesManager.setUserLoggedIn(newUser.email, newUser.firstName)

                setDefaultNutritionGoals()
                preferencesManager.setFirstTimeUser(false)

                onSuccess(newUser)
            } catch (e: Exception) {
                onError("Registration error: ${e.message}")
            }
        }
    }

    fun getUserByEmail(
        email: String,
        onSuccess: (User?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val user = repository.getUserByEmail(email)
                onSuccess(user)
            } catch (e: Exception) {
                onError("Error finding user: ${e.message}")
            }
        }
    }

    fun updateUser(
        user: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateUser(user)
                preferencesManager.setUserLoggedIn(user.email, user.firstName)
                onSuccess()
            } catch (e: Exception) {
                onError("Error updating user: ${e.message}")
            }
        }
    }

    fun logoutUser() {
        preferencesManager.setUserLoggedOut()
    }

    fun isUserLoggedIn(): Boolean {
        return preferencesManager.isUserLoggedIn()
    }

    fun getCurrentUserEmail(): String? {
        return preferencesManager.getUserEmail()
    }

    private fun setDefaultNutritionGoals() {
        preferencesManager.setDailyCalorieGoal(2000)
        preferencesManager.setDailyWaterGoal(2000)
        preferencesManager.setNotificationEnabled(true)
        preferencesManager.setWaterReminderInterval(60)
        preferencesManager.setMealReminderEnabled(true)
        preferencesManager.setPreferredUnits("metric")
    }
}