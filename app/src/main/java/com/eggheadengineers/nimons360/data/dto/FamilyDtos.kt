package com.eggheadengineers.nimons360.data.dto

import com.google.gson.annotations.SerializedName

data class FamilyMemberDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("joinedAt") val joinedAt: String?,
)

data class FamilySummaryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("memberCount") val memberCount: Int?,
    @SerializedName("familyCode") val familyCode: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("members") val members: List<FamilyMemberDto>?,
)

data class FamilyDetailDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("iconUrl") val iconUrl: String,
    @SerializedName("familyCode") val familyCode: String?,
    @SerializedName("isMember") val isMember: Boolean?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("members") val members: List<FamilyMemberDto>?,
)

data class CreateFamilyRequestDto(
    @SerializedName("name") val name: String,
    @SerializedName("iconUrl") val iconUrl: String,
)

data class JoinFamilyRequestDto(
    @SerializedName("familyId") val familyId: Int,
    @SerializedName("familyCode") val familyCode: String,
)

data class LeaveFamilyRequestDto(
    @SerializedName("familyId") val familyId: Int,
)

// Concrete (non-generic) response wrappers
data class FamilyListApiResponse(
    @SerializedName("data") val data: List<FamilySummaryDto>?,
)

data class FamilyDetailApiResponse(
    @SerializedName("data") val data: FamilyDetailDto?,
)
