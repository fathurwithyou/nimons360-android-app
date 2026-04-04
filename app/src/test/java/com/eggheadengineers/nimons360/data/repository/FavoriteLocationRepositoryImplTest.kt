package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.domain.model.FavoriteLocation
import com.eggheadengineers.nimons360.domain.repository.FavoriteLocationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteLocationRepositoryImplTest {

    private val repo = FakeFavoriteLocationRepository()

    @Test
    fun `add stores a favorite location`() = runTest {
        repo.add("Home", -6.89, 107.61)

        val locations = repo.observeAll().first()
        assertEquals(1, locations.size)
        assertEquals("Home", locations[0].name)
        assertEquals(-6.89, locations[0].lat, 0.001)
        assertEquals(107.61, locations[0].lng, 0.001)
    }

    @Test
    fun `add multiple favorites stores all`() = runTest {
        repo.add("Home", -6.89, 107.61)
        repo.add("School", -6.90, 107.62)
        repo.add("Office", -6.91, 107.63)

        val locations = repo.observeAll().first()
        assertEquals(3, locations.size)
    }

    @Test
    fun `delete removes the correct location`() = runTest {
        repo.add("Home", -6.89, 107.61)
        repo.add("School", -6.90, 107.62)

        val beforeDelete = repo.observeAll().first()
        assertEquals(2, beforeDelete.size)

        val homeId = beforeDelete.first { it.name == "Home" }.id
        repo.delete(homeId)

        val afterDelete = repo.observeAll().first()
        assertEquals(1, afterDelete.size)
        assertEquals("School", afterDelete[0].name)
    }

    @Test
    fun `delete non-existent id does not crash`() = runTest {
        repo.add("Home", -6.89, 107.61)
        repo.delete(9999)

        val locations = repo.observeAll().first()
        assertEquals(1, locations.size)
    }

    @Test
    fun `observeAll returns empty list initially`() = runTest {
        val locations = repo.observeAll().first()
        assertTrue(locations.isEmpty())
    }
}

class FakeFavoriteLocationRepository : FavoriteLocationRepository {
    private var nextId = 1L
    private val _locations = MutableStateFlow<List<FavoriteLocation>>(emptyList())

    override fun observeAll(): Flow<List<FavoriteLocation>> = _locations

    override suspend fun add(name: String, lat: Double, lng: Double) {
        _locations.update { current ->
            current + FavoriteLocation(
                id = nextId++,
                name = name,
                lat = lat,
                lng = lng,
                createdAt = System.currentTimeMillis(),
            )
        }
    }

    override suspend fun delete(id: Long) {
        _locations.update { current -> current.filter { it.id != id } }
    }
}
