package com.eggheadengineers.nimons360.feature.map

import app.cash.turbine.test
import com.eggheadengineers.nimons360.core.battery.BatteryState
import com.eggheadengineers.nimons360.core.network.NetworkStatus
import com.eggheadengineers.nimons360.domain.model.Family
import com.eggheadengineers.nimons360.domain.model.FamilyMember
import com.eggheadengineers.nimons360.domain.model.MemberPresence
import com.eggheadengineers.nimons360.domain.repository.FamilyRepository
import com.eggheadengineers.nimons360.domain.repository.PresenceRepository
import com.eggheadengineers.nimons360.core.battery.BatteryProvider
import com.eggheadengineers.nimons360.core.location.LocationTracker
import com.eggheadengineers.nimons360.core.network.ConnectivityObserver
import com.eggheadengineers.nimons360.core.sensor.OrientationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var fakePresenceRepo: FakePresenceRepository
    private lateinit var fakeFamilyRepo: FakeFamilyRepository
    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakePresenceRepo = FakePresenceRepository()
        fakeFamilyRepo = FakeFamilyRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MapViewModel {
        // We can't easily create a MapViewModel due to ConnectivityObserver/LocationTracker requiring Context.
        throw UnsupportedOperationException("ViewModel requires Android context dependencies")
    }

    @Test
    fun `filteredMembers returns all members when no family selected`() {
        val members = mapOf(
            "u1" to fakeMemberPresence("u1", "Alice"),
            "u2" to fakeMemberPresence("u2", "Bob"),
        )
        val state = MapUiState(members = members, selectedFamilyIds = emptySet())

        assertEquals(members, state.filteredMembers)
    }

    @Test
    fun `filteredMembers filters by selected family`() {
        val families = listOf(
            Family(
                id = "f1", name = "Family A", iconUrl = "",
                members = listOf(FamilyMember("u1", "Alice", "alice@test.com")),
            ),
            Family(
                id = "f2", name = "Family B", iconUrl = "",
                members = listOf(FamilyMember("u2", "Bob", "bob@test.com")),
            ),
        )
        val members = mapOf(
            "u1" to fakeMemberPresence("u1", "Alice"),
            "u2" to fakeMemberPresence("u2", "Bob"),
            "u3" to fakeMemberPresence("u3", "Charlie"),
        )
        val state = MapUiState(
            members = members,
            families = families,
            selectedFamilyIds = setOf("f1"),
        )

        val filtered = state.filteredMembers
        assertEquals(1, filtered.size)
        assertTrue(filtered.containsKey("u1"))
        assertFalse(filtered.containsKey("u2"))
        assertFalse(filtered.containsKey("u3"))
    }

    @Test
    fun `filteredMembers with multiple families selected includes union of members`() {
        val families = listOf(
            Family(
                id = "f1", name = "Family A", iconUrl = "",
                members = listOf(FamilyMember("u1", "Alice", "a@t.com")),
            ),
            Family(
                id = "f2", name = "Family B", iconUrl = "",
                members = listOf(FamilyMember("u2", "Bob", "b@t.com")),
            ),
        )
        val members = mapOf(
            "u1" to fakeMemberPresence("u1", "Alice"),
            "u2" to fakeMemberPresence("u2", "Bob"),
            "u3" to fakeMemberPresence("u3", "Charlie"),
        )
        val state = MapUiState(
            members = members,
            families = families,
            selectedFamilyIds = setOf("f1", "f2"),
        )

        val filtered = state.filteredMembers
        assertEquals(2, filtered.size)
        assertTrue(filtered.containsKey("u1"))
        assertTrue(filtered.containsKey("u2"))
    }

    @Test
    fun `filteredMembers returns empty when selected family has no matching members`() {
        val families = listOf(
            Family(
                id = "f1", name = "Family A", iconUrl = "",
                members = listOf(FamilyMember("u99", "Nobody", "n@t.com")),
            ),
        )
        val members = mapOf(
            "u1" to fakeMemberPresence("u1", "Alice"),
        )
        val state = MapUiState(
            members = members,
            families = families,
            selectedFamilyIds = setOf("f1"),
        )

        assertTrue(state.filteredMembers.isEmpty())
    }

    @Test
    fun `removeStaleMembers removes old entries`() {
        val now = System.currentTimeMillis()
        fakePresenceRepo.setMembers(
            mapOf(
                "u1" to fakeMemberPresence("u1", "Alice", lastSeen = now),
                "u2" to fakeMemberPresence("u2", "Bob", lastSeen = now - 10_000),
            )
        )

        fakePresenceRepo.removeStaleMembers(5000)

        val remaining = fakePresenceRepo.membersFlow.value
        assertEquals(1, remaining.size)
        assertTrue(remaining.containsKey("u1"))
    }

    @Test
    fun `removeStaleMembers keeps all fresh entries`() {
        val now = System.currentTimeMillis()
        fakePresenceRepo.setMembers(
            mapOf(
                "u1" to fakeMemberPresence("u1", "Alice", lastSeen = now),
                "u2" to fakeMemberPresence("u2", "Bob", lastSeen = now - 1000),
            )
        )

        fakePresenceRepo.removeStaleMembers(5000)

        assertEquals(2, fakePresenceRepo.membersFlow.value.size)
    }

    // ── Helper functions ──

    private fun fakeMemberPresence(
        userId: String,
        name: String,
        lastSeen: Long = System.currentTimeMillis(),
    ) = MemberPresence(
        userId = userId,
        name = name,
        email = "$name@test.com",
        lat = -6.89,
        lng = 107.61,
        rotation = 0f,
        battery = 80,
        charging = false,
        internetStatus = "wifi",
        lastSeen = lastSeen,
    )
}


class FakePresenceRepository : PresenceRepository {
    val membersFlow = MutableStateFlow<Map<String, MemberPresence>>(emptyMap())
    var connectCalled = false
    var disconnectCalled = false
    val sentPresences = mutableListOf<SentPresence>()

    data class SentPresence(
        val lat: Double, val lng: Double, val rotation: Float,
        val battery: Int, val charging: Boolean, val internetStatus: String,
    )

    fun setMembers(members: Map<String, MemberPresence>) {
        membersFlow.value = members
    }

    override fun connect() { connectCalled = true }
    override fun disconnect() { disconnectCalled = true }

    override fun sendPresence(
        lat: Double, lng: Double, rotation: Float,
        battery: Int, charging: Boolean, internetStatus: String,
    ) {
        sentPresences.add(SentPresence(lat, lng, rotation, battery, charging, internetStatus))
    }

    override fun observeMembers(): Flow<Map<String, MemberPresence>> = membersFlow

    override fun removeStaleMembers(maxAgeMs: Long) {
        val cutoff = System.currentTimeMillis() - maxAgeMs
        membersFlow.update { current ->
            current.filter { (_, m) -> m.lastSeen > cutoff }
        }
    }
}

class FakeFamilyRepository : FamilyRepository {
    private val familyChanges = MutableSharedFlow<Unit>()
    var families: List<Family> = emptyList()

    override suspend fun getAllFamilies(): Result<List<Family>> = Result.success(families)
    override suspend fun getMyFamilies(): Result<List<Family>> = Result.success(families)
    override suspend fun getDiscoverFamilies(): Result<List<Family>> = Result.success(emptyList())
    override suspend fun createFamily(name: String, iconUrl: String) = Result.failure<com.eggheadengineers.nimons360.domain.model.FamilyDetail>(Exception("not implemented"))
    override suspend fun getFamilyDetail(id: String) = Result.failure<com.eggheadengineers.nimons360.domain.model.FamilyDetail>(Exception("not implemented"))
    override suspend fun joinFamily(familyId: String, code: String) = Result.failure<Unit>(Exception("not implemented"))
    override suspend fun leaveFamily(familyId: String) = Result.failure<Unit>(Exception("not implemented"))
    override fun observeFamilyChanges(): Flow<Unit> = familyChanges
    override suspend fun pinFamily(family: Family) {}
    override suspend fun unpinFamily(familyId: String) {}
    override fun getPinnedFamilyIds(): Flow<Set<String>> = flowOf(emptySet())
}
