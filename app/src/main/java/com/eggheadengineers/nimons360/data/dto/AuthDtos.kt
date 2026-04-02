package com.eggheadengineers.nimons360.data.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class LoginUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nim") val nim: String?,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
)

data class LoginDataDto(
    @SerializedName("token") val token: String?,
    @SerializedName("expiresAt") val expiresAt: String?,
    @SerializedName("user") val user: LoginUserDto?,
)

data class ProfileDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nim") val nim: String?,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
)

data class UpdateProfileRequestDto(
    @SerializedName("fullName") val fullName: String,
)

// Concrete (non-generic) response wrappers — avoids Gson generic type erasure issues
data class LoginApiResponse(
    @SerializedName("data") val data: LoginDataDto?,
)

data class ProfileApiResponse(
    @SerializedName("data") val data: ProfileDto?,
)
