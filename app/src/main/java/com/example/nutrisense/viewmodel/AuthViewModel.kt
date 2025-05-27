package com.example.nutrisense.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrisense.data.database.AppDatabase
import com.example.nutrisense.data.entity.User
import com.example.nutrisense.data.repository.UserRepository
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository

    init {
        val userDao = AppDatabase.getDatabase(application).userDao()
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
                onSuccess()
            } catch (e: Exception) {
                onError("Error updating user: ${e.message}")
            }
        }
    }
}