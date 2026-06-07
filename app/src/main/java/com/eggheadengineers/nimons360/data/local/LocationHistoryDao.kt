package com.eggheadengineers.nimons360.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationHistoryDao {
    @Query("SELECT * FROM location_history ORDER BY recordedAt DESC")
    fun observeAll(): Flow<List<LocationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: LocationHistoryEntity)
}
