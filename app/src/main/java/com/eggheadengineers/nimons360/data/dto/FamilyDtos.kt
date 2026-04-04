package com.eggheadengineers.nimons360.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FamilyMemberDto(
    @SerialName("id") val id: Int?,
    @SerialName("fullName") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("joinedAt") val joinedAt: String?,
)

@Serializable
data class FamilySummaryDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("iconUrl") val iconUrl: String,
    @SerialName("memberCount") val memberCount: Int?,
    @SerialName("familyCode") val familyCode: String?,
    @SerialName("createdAt") val createdAt: String?,
    @SerialName("updatedAt") val updatedAt: String?,
    @SerialName("members") val members: List<FamilyMemberDto>?,
)

@Serializable
data class FamilyDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("iconUrl") val iconUrl: String,
    @SerialName("familyCode") val familyCode: String?,
    @SerialName("isMember") val isMember: Boolean?,
    @SerialName("createdAt") val createdAt: String?,
    @SerialName("updatedAt") val updatedAt: String?,
    @SerialName("members") val members: List<FamilyMemberDto>?,
)

@Serializable
data class CreateFamilyRequestDto(
    @SerialName("name") val name: String,
    @SerialName("iconUrl") val iconUrl: String,
)

@Serializable
data class JoinFamilyRequestDto(
    @SerialName("familyId") val familyId: Int,
    @SerialName("familyCode") val familyCode: String,
)

@Serializable
data class LeaveFamilyRequestDto(
    @SerialName("familyId") val familyId: Int,
)

// Concrete (non-generic) response wrappers
@Serializable
data class FamilyListApiResponse(
    @SerialName("data") val data: List<FamilySummaryDto>?,
)

@Serializable
data class FamilyDetailApiResponse(
    @SerialName("data") val data: FamilyDetailDto?,
)
