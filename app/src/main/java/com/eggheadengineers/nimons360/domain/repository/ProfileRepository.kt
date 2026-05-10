package com.eggheadengineers.nimons360.domain.repository

import com.eggheadengineers.nimons360.domain.model.Profile

interface ProfileRepository {
    suspend fun getProfile(): Result<Profile>
    suspend fun updateName(name: String): Result<Profile>
    suspend fun uploadPhoto(fileName: String, bytes: ByteArray, mediaType: String): Result<Profile>
}
