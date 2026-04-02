package com.eggheadengineers.nimons360.domain.repository

import com.eggheadengineers.nimons360.domain.model.MemberPresence
import kotlinx.coroutines.flow.Flow

interface PresenceRepository {
    fun connect()
    fun disconnect()
    fun sendPresence(lat: Double, lng: Double, rotation: Float, battery: Int, charging: Boolean, internetStatus: String)
    fun observeMembers(): Flow<Map<String, MemberPresence>>
    fun removeStaleMembers(maxAgeMs: Long)
}
