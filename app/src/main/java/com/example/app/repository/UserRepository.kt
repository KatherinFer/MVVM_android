package com.example.app.repository

import com.example.app.data.UserDao
import com.example.app.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {

    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)

    suspend fun refreshUsers() {
        // Simula una llamada a API con retraso
        delay(2000)
        val remoteUsers = listOf(
            User(1, "Ana García", "ana@email.com"),
            User(2, "Luis Pérez", "luis@email.com"),
            User(3, "Marta Díaz", "marta@email.com"),
            User(4, "Carlos Ruiz", "carlos@email.com")
        )
        // Guardamos en la base de datos (Fuente de verdad)
        userDao.insertUsers(remoteUsers)
    }
}
