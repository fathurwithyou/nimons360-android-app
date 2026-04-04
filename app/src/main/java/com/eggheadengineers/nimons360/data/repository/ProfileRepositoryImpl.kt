package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.data.dto.UpdateProfileRequestDto
import com.eggheadengineers.nimons360.data.network.ApiService
import com.eggheadengineers.nimons360.data.network.requireSuccess
import com.eggheadengineers.nimons360.domain.mapper.toDomain
import com.eggheadengineers.nimons360.domain.model.Profile
import com.eggheadengineers.nimons360.domain.repository.ProfileRepository

class ProfileRepositoryImpl(private val apiService: ApiService) : ProfileRepository {

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
}
