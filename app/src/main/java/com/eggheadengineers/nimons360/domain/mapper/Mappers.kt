package com.eggheadengineers.nimons360.domain.mapper

import com.eggheadengineers.nimons360.data.dto.*
import com.eggheadengineers.nimons360.data.local.PinnedFamilyEntity
import com.eggheadengineers.nimons360.domain.model.*

fun ProfileDto.toDomain() = Profile(id = id.toString(), name = fullName, email = email)

fun FamilyMemberDto.toDomain() = FamilyMember(id = id?.toString() ?: "", name = fullName, email = email)

fun FamilySummaryDto.toDomain(isPinned: Boolean = false) = Family(
    id = id.toString(),
    name = name,
    iconUrl = iconUrl,
    members = members?.map { it.toDomain() } ?: emptyList(),
    memberCount = memberCount ?: members?.size,
    isPinned = isPinned,
)

fun FamilyDetailDto.toDomain() = FamilyDetail(
    id = id.toString(),
    name = name,
    iconUrl = iconUrl,
    code = familyCode,
    members = members?.map { it.toDomain() } ?: emptyList(),
    isMember = isMember ?: false,
)

fun MemberPresenceUpdatedPayloadDto.toDomain() = MemberPresence(
    userId = userId?.toString() ?: id ?: "",
    name = fullName ?: "",
    email = email ?: "",
    lat = latitude ?: 0.0,
    lng = longitude ?: 0.0,
    rotation = rotation ?: 0f,
    battery = batteryLevel ?: 0,
    charging = isCharging ?: false,
    internetStatus = internetStatus ?: "unknown",
)

fun Family.toEntity() = PinnedFamilyEntity(id = id, name = name, iconUrl = iconUrl)

fun PinnedFamilyEntity.toDomain() = Family(id = id, name = name, iconUrl = iconUrl, isPinned = true)
