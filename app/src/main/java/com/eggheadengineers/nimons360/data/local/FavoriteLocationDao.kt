package com.eggheadengineers.nimons360.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {
    @Query("SELECT * FROM favorite_locations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavoriteLocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteLocationEntity)

    @Query("DELETE FROM favorite_locations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
