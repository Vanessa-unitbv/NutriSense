package com.example.nutrisense.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.data.entity.User
import com.example.nutrisense.data.repository.UserRepository
import com.example.nutrisense.managers.PreferencesRepository
import com.example.nutrisense.managers.SharedPreferencesManager
import com.example.nutrisense.utils.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: UserRepository,
    private val globalPreferencesManager: SharedPreferencesManager,
    private val preferencesRepository: PreferencesRepository,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()

    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData.asStateFlow()

    fun loginUser(email: String, password: String) {
        val validation = validateLoginInput(email, password)
        if (validation is ValidationResult.Error) {
            _loginState.value = LoginState.Error(validation.message)
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            try {
                val user = repository.loginUser(email, password)
                if (user != null) {
                    saveUserSessionAsync(user)
                    _loginState.value = LoginState.Success(user)
                } else {
                    _loginState.value = LoginState.Error("Invalid email or password")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error", e)
                _loginState.value = LoginState.Error("Login error: ${e.message}")
            }
        }
    }

    fun registerUser(
        email: String,
        password: String,
        firstName: String?,
        lastName: String?,
        age: Int?
    ) {
        val validation = validateRegistrationInput(email, password, age)
        if (validation is ValidationResult.Error) {
            _registerState.value = RegisterState.Error(validation.message)
            return
        }

        _registerState.value = RegisterState.Loading

        viewModelScope.launch {
            try {
                if (repository.isEmailExists(email)) {
                    _registerState.value = RegisterState.Error("Email is already registered")
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

                saveUserSessionAsync(newUser)
                setDefaultUserSettingsAsync(email, age)

                _registerState.value = RegisterState.Success(newUser)

            } catch (e: Exception) {
                Log.e(TAG, "Registration error", e)
                _registerState.value = RegisterState.Error("Registration error: ${e.message}")
            }
        }
    }

    fun getUserByEmail(email: String) {
        viewModelScope.launch {
            try {
                val user = repository.getUserByEmail(email)
                _userData.value = user
            } catch (e: Exception) {
                Log.e(TAG, "Error getting user by email", e)
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                repository.updateUser(user)
                globalPreferencesManager.setUserLoggedIn(user.email, user.firstName)

                user.age?.let { age ->
                    val userPrefs = preferencesRepository.getManagerForUser(user.email)
                    userPrefs.setUserAge(age)
                }

                _userData.value = user
            } catch (e: Exception) {
                Log.e(TAG, "Error updating user", e)
            }
        }
    }

    fun logoutUserAsync() {
        viewModelScope.launch {
            try {
                globalPreferencesManager.setUserLoggedOut()
                _loginState.value = LoginState.Idle
                _userData.value = null
            } catch (e: Exception) {
                Log.e(TAG, "Error logging out user", e)
            }
        }
    }

    fun logoutUser() {
        globalPreferencesManager.setUserLoggedOut()
        _loginState.value = LoginState.Idle
        _userData.value = null
    }

    fun isUserLoggedIn(): Boolean {
        return globalPreferencesManager.isUserLoggedIn()
    }

    fun getCurrentUserEmail(): String? {
        return globalPreferencesManager.getUserEmail()
    }

    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }

    suspend fun checkIfEmailExists(email: String): Boolean {
        return try {
            repository.isEmailExists(email)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if email exists", e)
            false
        }
    }

    private suspend fun saveUserSessionAsync(user: User) {
        try {
            globalPreferencesManager.setUserLoggedIn(user.email, user.firstName)
            SharedPreferencesManager.setCurrentUser(user.email)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user session", e)
        }
    }

    private suspend fun setDefaultUserSettingsAsync(email: String, age: Int?) {
        try {
            val userPrefs = preferencesRepository.getManagerForUser(email)
            userPrefs.clearUserData()

            // Setari directe - sunt operatii sincrone rapide
            userPrefs.setDailyCalorieGoal(AppConstants.DEFAULT_CALORIE_GOAL)
            userPrefs.setDailyWaterGoal(AppConstants.DEFAULT_WATER_GOAL_ML)
            userPrefs.setNotificationEnabled(true)
            userPrefs.setWaterReminderInterval(AppConstants.DEFAULT_WATER_REMINDER_INTERVAL)
            userPrefs.setMealReminderEnabled(true)
            userPrefs.setPreferredUnits(AppConstants.DEFAULT_UNITS)
            age?.let { userPrefs.setUserAge(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting default user settings", e)
        }
    }

    private fun validateLoginInput(email: String, password: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            password.isBlank() -> ValidationResult.Error("Password is required")
            else -> ValidationResult.Success
        }
    }

    private fun validateRegistrationInput(
        email: String,
        password: String,
        age: Int?
    ): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Error("Email is required")
            password.isBlank() -> ValidationResult.Error("Password is required")
            password.length < AppConstants.MIN_PASSWORD_LENGTH -> {
                ValidationResult.Error("Password must be at least ${AppConstants.MIN_PASSWORD_LENGTH} characters")
            }
            age != null && (age < AppConstants.MIN_AGE || age > AppConstants.MAX_AGE) -> {
                ValidationResult.Error("Age must be between ${AppConstants.MIN_AGE} and ${AppConstants.MAX_AGE}")
            }
            else -> ValidationResult.Success
        }
    }


    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: User) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    sealed class RegisterState {
        object Idle : RegisterState()
        object Loading : RegisterState()
        data class Success(val user: User) : RegisterState()
        data class Error(val message: String) : RegisterState()
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}