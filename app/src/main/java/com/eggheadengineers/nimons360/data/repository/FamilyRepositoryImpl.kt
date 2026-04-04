package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.data.dto.CreateFamilyRequestDto
import com.eggheadengineers.nimons360.data.dto.JoinFamilyRequestDto
import com.eggheadengineers.nimons360.data.dto.LeaveFamilyRequestDto
import com.eggheadengineers.nimons360.data.local.AppDatabase
import com.eggheadengineers.nimons360.data.local.PinnedFamilyEntity
import com.eggheadengineers.nimons360.data.network.ApiService
import com.eggheadengineers.nimons360.data.network.requireSuccess
import com.eggheadengineers.nimons360.domain.mapper.toDomain
import com.eggheadengineers.nimons360.domain.model.Family
import com.eggheadengineers.nimons360.domain.model.FamilyDetail
import com.eggheadengineers.nimons360.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class FamilyRepositoryImpl(
    private val apiService: ApiService,
    private val db: AppDatabase,
) : FamilyRepository {

    private val dao = db.pinnedFamilyDao()
    private val familyChanges = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private suspend fun pinnedIds(): Set<String> = dao.observeIds().first().toSet()

    override suspend fun getAllFamilies(): Result<List<Family>> = runCatching {
        val pinned = pinnedIds()
        val response = apiService.getAllFamilies()
        response.requireSuccess("Failed to load families")
        response.body()?.data?.map { it.toDomain(isPinned = it.id.toString() in pinned) } ?: emptyList()
    }

    override suspend fun getMyFamilies(): Result<List<Family>> = runCatching {
        val pinned = pinnedIds()
        val response = apiService.getMyFamilies()
        response.requireSuccess("Failed to load your families")
        response.body()?.data?.map { it.toDomain(isPinned = it.id.toString() in pinned) } ?: emptyList()
    }

    override suspend fun getDiscoverFamilies(): Result<List<Family>> = runCatching {
        val pinned = pinnedIds()
        val response = apiService.getDiscoverFamilies()
        response.requireSuccess("Failed to load discover families")
        response.body()?.data?.map { it.toDomain(isPinned = it.id.toString() in pinned) } ?: emptyList()
    }

    override suspend fun getFamilyDetail(id: String): Result<FamilyDetail> = runCatching {
        val response = apiService.getFamilyDetail(id)
        response.requireSuccess("Failed to load family detail")
        response.body()?.data?.toDomain() ?: error("Empty detail response")
    }

    override suspend fun createFamily(name: String, iconUrl: String): Result<FamilyDetail> = runCatching {
        val response = apiService.createFamily(CreateFamilyRequestDto(name, iconUrl))
        response.requireSuccess("Failed to create family")
        val detail = response.body()?.data?.toDomain() ?: error("Empty create response")
        familyChanges.tryEmit(Unit)
        detail
    }

    override suspend fun joinFamily(familyId: String, code: String): Result<Unit> = runCatching {
        val response = apiService.joinFamily(
            JoinFamilyRequestDto(
                familyId = familyId.toIntOrNull() ?: error("Invalid familyId"),
                familyCode = code,
            )
        )
        response.requireSuccess("Failed to join family")
        familyChanges.tryEmit(Unit)
    }

    override suspend fun leaveFamily(familyId: String): Result<Unit> = runCatching {
        val response = apiService.leaveFamily(
            LeaveFamilyRequestDto(familyId = familyId.toIntOrNull() ?: error("Invalid familyId"))
        )
        response.requireSuccess("Failed to leave family")
        familyChanges.tryEmit(Unit)
    }

    override suspend fun pinFamily(family: Family) {
        dao.insert(PinnedFamilyEntity(family.id, family.name, family.iconUrl))
    }

    override suspend fun unpinFamily(familyId: String) {
        dao.deleteById(familyId)
    }

    override fun getPinnedFamilyIds(): Flow<Set<String>> =
        dao.observeIds().map { it.toSet() }

    override fun observeFamilyChanges(): Flow<Unit> = familyChanges
}
