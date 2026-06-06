package com.example.iucampus.data.auth

import kotlinx.coroutines.delay

class AuthRepository {
    // Simulated remote authentication
    suspend fun login(username: String, password: String): Boolean {
        delay(1000) // Simulate network delay
        // Accept any non-empty username/password for sandbox showcase
        return username.isNotBlank() && password.isNotBlank()
    }
}
