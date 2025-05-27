package com.example.nutrisense.data.repository

import com.example.nutrisense.data.dao.UserDao
import com.example.nutrisense.data.entity.User

class UserRepository(private val userDao: UserDao) {

    suspend fun loginUser(email: String, password: String): User? {
        return userDao.getUserByEmailAndPassword(email, password)
    }

    suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }

    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }

    suspend fun isEmailExists(email: String): Boolean {
        return userDao.getUserCountByEmail(email) > 0
    }

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }
}