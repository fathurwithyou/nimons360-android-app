package com.eggheadengineers.nimons360.domain.repository

import com.eggheadengineers.nimons360.domain.model.LiveStream
import kotlinx.coroutines.flow.Flow

interface LiveStreamRepository {
    suspend fun startStream(familyId: String, title: String?): Result<LiveStream>
    suspend fun endStream(streamId: String): Result<Unit>
    suspend fun listStreams(familyId: String): Result<List<LiveStream>>

    fun observeFamilyStreams(familyId: String): Flow<List<LiveStream>>
    fun connect()
    fun disconnect()
}
