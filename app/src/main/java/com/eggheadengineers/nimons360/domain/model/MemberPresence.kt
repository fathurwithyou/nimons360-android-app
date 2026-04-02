package com.eggheadengineers.nimons360.domain.model

data class MemberPresence(
    val userId: String,
    val name: String,
    val email: String,
    val lat: Double,
    val lng: Double,
    val rotation: Float,
    val battery: Int,
    val charging: Boolean,
    val internetStatus: String,
    val lastSeen: Long = System.currentTimeMillis(),
)
