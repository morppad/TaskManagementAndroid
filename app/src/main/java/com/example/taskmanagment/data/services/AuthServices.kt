package com.example.taskmanagment.data.services

import android.util.Log
import at.favre.lib.crypto.bcrypt.BCrypt
import io.github.jan.supabase.postgrest.from
import com.example.taskmanagment.data.models.User

    suspend fun loginUser(
        email: String,
        password: String,
        onSuccess: (String, String) -> Unit, // user_id and role
        onError: (String) -> Unit
    ) {
        try {
            // Fetch all users from the database
            val users = supabaseClient.from("users")
                .select()
                .decodeList<User>()

            // Find user by email
            val user = users.find { it.email == email }
            if (user == null) {
                onError("User not found")
                return
            }

            val hashedPassword = user.password
            val role = user.role
            val user_id = user.id

            // Verify password using bcrypt
            val result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword)
            if (result.verified) {
                onSuccess(user_id.toString(), role) // Return user_id and role on success
            } else {
                onError("Invalid password")
            }
        } catch (e: Exception) {
            onError(e.message ?: "Login failed")
        }
    }

    suspend fun registerUser(
        email: String,
        password: String,
        username: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            // Hash the password
            val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())

            // Insert new user into the database
            supabaseClient.from("users").insert(
                mapOf(
                    "email" to email,
                    "username" to username,
                    "password_hash" to hashedPassword,
                    "role" to "user" // Default role
                )
            )
            onSuccess()
        } catch (e: Exception) {
            Log.e("AuthService", "Registration failed", e)
            onError(e.message ?: "Registration failed")
        }
    }
