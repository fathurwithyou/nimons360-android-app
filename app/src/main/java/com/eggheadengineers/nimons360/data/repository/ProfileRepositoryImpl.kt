package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.data.dto.UpdateProfileRequestDto
import com.eggheadengineers.nimons360.data.network.ApiService
import com.eggheadengineers.nimons360.data.network.requireSuccess
import com.eggheadengineers.nimons360.domain.mapper.toDomain
import com.eggheadengineers.nimons360.domain.model.Profile
import com.eggheadengineers.nimons360.domain.repository.ProfileRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileRepositoryImpl(private val apiService: ApiService) : ProfileRepository {
    companion object {
        private const val MAX_PROFILE_PHOTO_BYTES = 500 * 1024
    }

    override suspend fun getProfile(): Result<Profile> = runCatching {
        val response = apiService.getProfile()
        response.requireSuccess("Failed to get profile")
        response.body()?.data?.toDomain() ?: error("Empty profile response")
    }

    override suspend fun updateName(name: String): Result<Profile> = runCatching {
        val response = apiService.updateProfile(UpdateProfileRequestDto(fullName = name))
        response.requireSuccess("Failed to update profile")
        response.body()?.data?.toDomain() ?: error("Empty update response")
    }

    override suspend fun uploadPhoto(
        fileName: String,
        bytes: ByteArray,
        mediaType: String,
    ): Result<Profile> = runCatching {
        require(mediaType == "image/png" || mediaType == "image/jpeg") {
            "Profile photo must be a PNG or JPEG image"
        }
        require(bytes.size <= MAX_PROFILE_PHOTO_BYTES) {
            "Profile photo must be 500 KB or smaller"
        }

        val body = bytes.toRequestBody(mediaType.toMediaType())
        val part = MultipartBody.Part.createFormData("photo", fileName, body)
        val response = apiService.uploadProfilePhoto(part)
        response.requireSuccess("Failed to upload profile photo")
        response.body()?.data?.toDomain() ?: error("Empty photo upload response")
    }
}
