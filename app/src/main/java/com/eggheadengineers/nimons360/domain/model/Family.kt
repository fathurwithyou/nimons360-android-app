package com.eggheadengineers.nimons360.domain.model

data class Family(
    val id: String,
    val name: String,
    val iconUrl: String,
    val members: List<FamilyMember> = emptyList(),
    val memberCount: Int? = null,
    val isPinned: Boolean = false,
)
