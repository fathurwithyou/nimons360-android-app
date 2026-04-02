package com.eggheadengineers.nimons360.domain.mapper

import com.eggheadengineers.nimons360.data.dto.FamilyMemberDto
import com.eggheadengineers.nimons360.data.dto.FamilySummaryDto
import com.eggheadengineers.nimons360.data.dto.MemberPresenceUpdatedPayloadDto
import com.eggheadengineers.nimons360.data.dto.ProfileDto
import org.junit.Assert.*
import org.junit.Test

class MappersTest {

    // ── MemberPresenceUpdatedPayloadDto.toDomain() ──

    @Test
    fun `toDomain maps all fields correctly`() {
        val dto = MemberPresenceUpdatedPayloadDto(
            userId = "u1",
            id = null,
            fullName = "Alice Smith",
            email = "alice@test.com",
            latitude = -6.891,
            longitude = 107.612,
            rotation = 180f,
            batteryLevel = 92,
            isCharging = true,
            internetStatus = "wifi",
            metadata = mapOf("key" to "value"),
        )
        val domain = dto.toDomain()

        assertEquals("u1", domain.userId)
        assertEquals("Alice Smith", domain.name)
        assertEquals("alice@test.com", domain.email)
        assertEquals(-6.891, domain.lat, 0.0001)
        assertEquals(107.612, domain.lng, 0.0001)
        assertEquals(180f, domain.rotation, 0.01f)
        assertEquals(92, domain.battery)
        assertTrue(domain.charging)
        assertEquals("wifi", domain.internetStatus)
    }

    @Test
    fun `toDomain falls back to id when userId is null`() {
        val dto = MemberPresenceUpdatedPayloadDto(
            userId = null,
            id = "fallback-id",
            fullName = "Bob",
            email = null,
            latitude = null,
            longitude = null,
            rotation = null,
            batteryLevel = null,
            isCharging = null,
            internetStatus = null,
            metadata = null,
        )
        val domain = dto.toDomain()
        assertEquals("fallback-id", domain.userId)
    }

    @Test
    fun `toDomain returns empty userId when both userId and id are null`() {
        val dto = MemberPresenceUpdatedPayloadDto(
            userId = null, id = null, fullName = null, email = null,
            latitude = null, longitude = null, rotation = null,
            batteryLevel = null, isCharging = null, internetStatus = null,
            metadata = null,
        )
        val domain = dto.toDomain()
        assertEquals("", domain.userId)
        assertEquals("", domain.name)
        assertEquals(0.0, domain.lat, 0.001)
        assertEquals(0, domain.battery)
        assertFalse(domain.charging)
        assertEquals("unknown", domain.internetStatus)
    }

    // ── ProfileDto.toDomain() ──

    @Test
    fun `ProfileDto toDomain maps correctly`() {
        val dto = ProfileDto(
            id = 42, fullName = "Charlie", email = "charlie@test.com",
            nim = null, createdAt = null, updatedAt = null,
        )
        val domain = dto.toDomain()
        assertEquals("42", domain.id)
        assertEquals("Charlie", domain.name)
        assertEquals("charlie@test.com", domain.email)
    }

    // ── FamilyMemberDto.toDomain() ──

    @Test
    fun `FamilyMemberDto toDomain maps correctly`() {
        val dto = FamilyMemberDto(id = 7, fullName = "Diana", email = "diana@test.com", joinedAt = null)
        val domain = dto.toDomain()
        assertEquals("7", domain.id)
        assertEquals("Diana", domain.name)
        assertEquals("diana@test.com", domain.email)
    }

    @Test
    fun `FamilyMemberDto with null id returns empty string`() {
        val dto = FamilyMemberDto(id = null, fullName = "Eve", email = "eve@test.com", joinedAt = null)
        val domain = dto.toDomain()
        assertEquals("", domain.id)
    }

    // ── FamilySummaryDto.toDomain() ──

    @Test
    fun `FamilySummaryDto toDomain maps correctly with members`() {
        val dto = FamilySummaryDto(
            id = 1,
            name = "Test Family",
            iconUrl = "https://example.com/icon.png",
            members = listOf(
                FamilyMemberDto(id = 10, fullName = "Alice", email = "a@t.com", joinedAt = null),
                FamilyMemberDto(id = 11, fullName = "Bob", email = "b@t.com", joinedAt = null),
            ),
            memberCount = 2,
            familyCode = null,
            createdAt = null,
            updatedAt = null,
        )
        val domain = dto.toDomain(isPinned = true)
        assertEquals("1", domain.id)
        assertEquals("Test Family", domain.name)
        assertEquals(2, domain.members.size)
        assertEquals("Alice", domain.members[0].name)
        assertTrue(domain.isPinned)
        assertEquals(2, domain.memberCount)
    }

    @Test
    fun `FamilySummaryDto with null members returns empty list`() {
        val dto = FamilySummaryDto(
            id = 2,
            name = "Empty Family",
            iconUrl = "",
            members = null,
            memberCount = null,
            familyCode = null,
            createdAt = null,
            updatedAt = null,
        )
        val domain = dto.toDomain()
        assertTrue(domain.members.isEmpty())
        assertFalse(domain.isPinned)
        assertNull(domain.memberCount)
    }
}
