package com.eggheadengineers.nimons360.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedFamilyDao {
    @Query("SELECT * FROM pinned_families")
    fun observeAll(): Flow<List<PinnedFamilyEntity>>

    @Query("SELECT id FROM pinned_families")
    fun observeIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PinnedFamilyEntity)

    @Query("DELETE FROM pinned_families WHERE id = :id")
    suspend fun deleteById(id: String)
}
