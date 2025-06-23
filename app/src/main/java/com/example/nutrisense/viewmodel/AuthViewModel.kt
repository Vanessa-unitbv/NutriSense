package com.example.nutrisense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.activities.ApplicationController
import com.example.nutrisense.data.entity.User
import com.example.nutrisense.data.repository.UserRepository
import com.example.nutrisense.managers.SharedPreferencesManager
import com.example.nutrisense.utils.AppConstants
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository
    private val appController: ApplicationController = ApplicationController.instance
    private val globalPreferencesManager: SharedPreferencesManager = appController.globalPreferencesManager

    init {
        val userDao = appController.database.userDao()
        repository = UserRepository(userDao)
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
                    globalPreferencesManager.setUserLoggedIn(user.email, user.firstName)

                    val userPreferencesManager = SharedPreferencesManager.getInstance(
                        getApplication(),
                        user.email
                    )

                    if (userPreferencesManager.isFirstTimeUser()) {
                        setDefaultNutritionGoals(userPreferencesManager)
                        userPreferencesManager.setFirstTimeUser(false)
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

        if (password.length < AppConstants.MIN_PASSWORD_LENGTH) {
            onError("Password must be at least ${AppConstants.MIN_PASSWORD_LENGTH} characters")
            return
        }

        age?.let { userAge ->
            if (userAge < AppConstants.MIN_AGE || userAge > AppConstants.MAX_AGE) {
                onError("Age must be between ${AppConstants.MIN_AGE} and ${AppConstants.MAX_AGE}")
                return
            }
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

                globalPreferencesManager.setUserLoggedIn(newUser.email, newUser.firstName)

                val userPreferencesManager = SharedPreferencesManager.getInstance(
                    getApplication(),
                    newUser.email
                )

                setDefaultNutritionGoals(userPreferencesManager)
                userPreferencesManager.setFirstTimeUser(false)

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
                globalPreferencesManager.setUserLoggedIn(user.email, user.firstName)
                onSuccess()
            } catch (e: Exception) {
                onError("Error updating user: ${e.message}")
            }
        }
    }

    fun logoutUser() {
        globalPreferencesManager.setUserLoggedOut()
    }

    fun isUserLoggedIn(): Boolean {
        return globalPreferencesManager.isUserLoggedIn()
    }

    fun getCurrentUserEmail(): String? {
        return globalPreferencesManager.getUserEmail()
    }

    private fun setDefaultNutritionGoals(preferencesManager: SharedPreferencesManager) {
        preferencesManager.setDailyCalorieGoal(AppConstants.DEFAULT_CALORIE_GOAL)
        preferencesManager.setDailyWaterGoal(AppConstants.DEFAULT_WATER_GOAL_ML)
        preferencesManager.setNotificationEnabled(true)
        preferencesManager.setWaterReminderInterval(AppConstants.DEFAULT_WATER_REMINDER_INTERVAL)
        preferencesManager.setMealReminderEnabled(true)
        preferencesManager.setPreferredUnits(AppConstants.DEFAULT_UNITS)
        preferencesManager.setActivityLevel(AppConstants.DEFAULT_ACTIVITY_LEVEL)
    }
}