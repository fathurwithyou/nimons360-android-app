package com.eggheadengineers.nimons360.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteLocationDao {
    @Transaction
    @Query("SELECT * FROM favorite_locations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<FavoriteLocationWithPhotos>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(entity: FavoriteLocationEntity): Long

    @Update
    suspend fun updateLocation(entity: FavoriteLocationEntity)

    @Query("SELECT * FROM favorite_locations WHERE id = :id LIMIT 1")
    suspend fun getLocationById(id: Long): FavoriteLocationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(entity: FavoriteLocationPhotoEntity)

    @Query("SELECT path FROM favorite_location_photos WHERE locationId = :locationId")
    suspend fun getPhotoPaths(locationId: Long): List<String>

    @Query("DELETE FROM favorite_location_photos WHERE locationId = :locationId")
    suspend fun deletePhotosByLocationId(locationId: Long)

    @Query("DELETE FROM favorite_locations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun deleteLocationWithPhotos(id: Long): List<String> {
        val paths = getPhotoPaths(id)
        deletePhotosByLocationId(id)
        deleteById(id)
        return paths
    }
}
