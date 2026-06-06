package com.eggheadengineers.nimons360.domain.repository

import com.eggheadengineers.nimons360.domain.model.Family
import com.eggheadengineers.nimons360.domain.model.FamilyDetail
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    suspend fun getAllFamilies(): Result<List<Family>>
    suspend fun getMyFamilies(): Result<List<Family>>
    suspend fun getDiscoverFamilies(): Result<List<Family>>
    suspend fun getFamilyDetail(id: String): Result<FamilyDetail>
    suspend fun createFamily(name: String, iconUrl: String): Result<FamilyDetail>
    suspend fun joinFamily(familyId: String, code: String): Result<Unit>
    suspend fun leaveFamily(familyId: String): Result<Unit>
    suspend fun pinFamily(family: Family)
    suspend fun unpinFamily(familyId: String)
    fun getPinnedFamilyIds(): Flow<Set<String>>
    fun observeFamilyChanges(): Flow<Unit>
    fun notifyMemberChanged()
}
