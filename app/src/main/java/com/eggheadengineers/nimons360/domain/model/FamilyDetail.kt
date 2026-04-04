package com.eggheadengineers.nimons360.domain.model

data class FamilyDetail(
    val id: String,
    val name: String,
    val iconUrl: String,
    val code: String?,
    val members: List<FamilyMember>,
    val isMember: Boolean,
)
