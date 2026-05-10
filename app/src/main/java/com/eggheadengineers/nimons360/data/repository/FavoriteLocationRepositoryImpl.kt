package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.core.files.FavoriteLocationPhotoStore
import com.eggheadengineers.nimons360.data.local.FavoriteLocationDao
import com.eggheadengineers.nimons360.data.local.FavoriteLocationEntity
import com.eggheadengineers.nimons360.data.local.FavoriteLocationPhotoEntity
import com.eggheadengineers.nimons360.data.local.FavoriteLocationWithPhotos
import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.domain.model.FavoriteLocationPhotoInput
import com.eggheadengineers.nimons360.domain.repository.FavoriteLocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteLocationRepositoryImpl(
    private val dao: FavoriteLocationDao,
    private val photoStore: FavoriteLocationPhotoStore,
) : FavoriteLocationRepository {

    override fun observeAll(): Flow<List<FavoriteLocation>> =
        dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun add(
        name: String,
        description: String,
        lat: Double,
        lng: Double,
        photos: List<FavoriteLocationPhotoInput>,
    ) {
        val now = System.currentTimeMillis()
        val locationId = dao.insertLocation(
            FavoriteLocationEntity(
                name = name,
                description = description,
                lat = lat,
                lng = lng,
                createdAt = now,
                updatedAt = now,
            )
        )
        savePhotos(locationId, photos)
    }

    override suspend fun update(
        id: Long,
        name: String,
        description: String,
        photosToAdd: List<FavoriteLocationPhotoInput>,
    ) {
        val current = dao.getLocationById(id) ?: return
        dao.updateLocation(
            current.copy(
                name = name,
                description = description,
                updatedAt = System.currentTimeMillis(),
            )
        )
        savePhotos(id, photosToAdd)
    }

    override suspend fun delete(id: Long) {
        val paths = dao.deleteLocationWithPhotos(id)
        photoStore.deleteAll(paths)
    }

    private suspend fun savePhotos(locationId: Long, photos: List<FavoriteLocationPhotoInput>) {
        photos.forEach { input ->
            val path = photoStore.save(locationId, input.fileName, input.bytes)
            dao.insertPhoto(FavoriteLocationPhotoEntity(locationId = locationId, path = path))
        }
    }

    private fun FavoriteLocationWithPhotos.toDomain() = FavoriteLocation(
        id = location.id,
        name = location.name,
        description = location.description,
        lat = location.lat,
        lng = location.lng,
        photoPaths = photos.map { it.path },
        createdAt = location.createdAt,
        updatedAt = location.updatedAt,
    )
}
