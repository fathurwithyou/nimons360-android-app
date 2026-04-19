package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.core.session.SessionManager
import com.eggheadengineers.nimons360.data.dto.LoginRequestDto
import com.eggheadengineers.nimons360.data.network.ApiService
import com.eggheadengineers.nimons360.data.network.requireSuccess
import com.eggheadengineers.nimons360.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<String> = runCatching {
        val response = apiService.login(LoginRequestDto(email, password))
        response.requireSuccess("Login failed")
        val loginData = response.body()?.data ?: error("Empty login response")
        val token = loginData.token ?: error("Token not in response")
        sessionManager.saveToken(token)
        loginData.user?.fullName?.let { sessionManager.saveUserName(it) }
        loginData.user?.id?.let { sessionManager.saveUserId(it.toString()) }
        token
    }

    override suspend fun getToken(): String? = sessionManager.getToken()

    override suspend fun logout() = sessionManager.clearToken()

    override suspend fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()
}
