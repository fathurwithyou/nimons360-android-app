package com.eggheadengineers.nimons360.domain.repository

import com.eggheadengineers.nimons360.domain.model.Profile

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<String>
    suspend fun getToken(): String?
    suspend fun logout()
    suspend fun isLoggedIn(): Boolean
}
