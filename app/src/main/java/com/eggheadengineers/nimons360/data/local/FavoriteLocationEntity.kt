package com.eggheadengineers.nimons360.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "favorite_locations")
data class FavoriteLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val lat: Double,
    val lng: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
)

@Entity(
    tableName = "favorite_location_photos",
    foreignKeys = [
        ForeignKey(
            entity = FavoriteLocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("locationId")],
)
data class FavoriteLocationPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val locationId: Long,
    val path: String,
    val createdAt: Long = System.currentTimeMillis(),
)

data class FavoriteLocationWithPhotos(
    @Embedded val location: FavoriteLocationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "locationId",
    )
    val photos: List<FavoriteLocationPhotoEntity>,
)
