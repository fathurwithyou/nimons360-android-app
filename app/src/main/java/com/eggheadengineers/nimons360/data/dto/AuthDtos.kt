package com.eggheadengineers.nimons360.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    @SerialName("email") val email: String,
    @SerialName("password") val password: String,
)

@Serializable
data class LoginUserDto(
    @SerialName("id") val id: Int,
    @SerialName("nim") val nim: String?,
    @SerialName("email") val email: String,
    @SerialName("fullName") val fullName: String,
)

@Serializable
data class LoginDataDto(
    @SerialName("token") val token: String?,
    @SerialName("expiresAt") val expiresAt: String?,
    @SerialName("user") val user: LoginUserDto?,
)

@Serializable
data class ProfileDto(
    @SerialName("id") val id: Int,
    @SerialName("nim") val nim: String?,
    @SerialName("email") val email: String,
    @SerialName("fullName") val fullName: String,
    @SerialName("createdAt") val createdAt: String?,
    @SerialName("updatedAt") val updatedAt: String?,
)

@Serializable
data class UpdateProfileRequestDto(
    @SerialName("fullName") val fullName: String,
)

// Concrete (non-generic) response wrappers
@Serializable
data class LoginApiResponse(
    @SerialName("data") val data: LoginDataDto?,
)

@Serializable
data class ProfileApiResponse(
    @SerialName("data") val data: ProfileDto?,
)
