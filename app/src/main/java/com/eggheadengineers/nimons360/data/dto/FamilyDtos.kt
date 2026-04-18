package com.eggheadengineers.nimons360.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FamilyMemberDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("fullName") val fullName: String,
    @SerialName("email") val email: String,
    @SerialName("joinedAt") val joinedAt: String? = null,
)

@Serializable
data class FamilySummaryDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("iconUrl") val iconUrl: String,
    @SerialName("memberCount") val memberCount: Int? = null,
    @SerialName("familyCode") val familyCode: String? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("members") val members: List<FamilyMemberDto>? = null,
)

@Serializable
data class FamilyDetailDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String,
    @SerialName("iconUrl") val iconUrl: String,
    @SerialName("familyCode") val familyCode: String? = null,
    @SerialName("isMember") val isMember: Boolean? = null,
    @SerialName("createdAt") val createdAt: String? = null,
    @SerialName("updatedAt") val updatedAt: String? = null,
    @SerialName("members") val members: List<FamilyMemberDto>? = null,
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

@Serializable
data class SimpleApiResponse(
    @SerialName("status") val status: String? = null,
    @SerialName("message") val message: String? = null,
)
