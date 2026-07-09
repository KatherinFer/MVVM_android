package com.example.app.repository

import com.example.app.data.UserDao
import com.example.app.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.net.SocketTimeoutException

class NetworkException(message: String) : IOException(message)
class ServerException(message: String) : Exception(message)

class UserRepository(private val userDao: UserDao) {

    val allUsers: Flow<List<User>> = userDao.getAllUsers()

    suspend fun getUserById(id: Int): User? = userDao.getUserById(id)

    suspend fun refreshUsers() {
        // Simulación de los 3 errores de la guía (Aleatorios y Fijos)
        val random = (1..10).random()
        when (random) {
            1 -> throw NetworkException("Error de red: No se pudo establecer conexión")
            2 -> throw SocketTimeoutException("Error de tiempo: El servidor no respondió a tiempo")
            3 -> throw ServerException("Error de servidor: Respuesta inválida (500)")
        }

        // Si no hay error, esperamos 1 segundo y descargamos datos
        delay(1000)
        val remoteUsers = listOf(
            User(1, "Ana García", "ana@email.com"),
            User(2, "Luis Pérez", "luis@email.com"),
            User(3, "Marta Díaz", "marta@email.com"),
            User(4, "Carlos Ruiz", "carlos@email.com")
        )
        userDao.insertUsers(remoteUsers)
    }
}
