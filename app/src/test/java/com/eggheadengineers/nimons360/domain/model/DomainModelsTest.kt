package com.eggheadengineers.nimons360.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainModelsTest {

    @Test
    fun `Family uses default empty members, null memberCount, and unpinned state`() {
        val family = Family(
            id = "f1",
            name = "Test Family",
            iconUrl = "https://example.com/family.png",
        )

        assertTrue(family.members.isEmpty())
        assertEquals(null, family.memberCount)
        assertFalse(family.isPinned)
    }

    @Test
    fun `Family keeps explicit members, memberCount, and pinned state`() {
        val members = listOf(
            FamilyMember(id = "m1", name = "Alice", email = "alice@test.com"),
            FamilyMember(id = "m2", name = "Bob", email = "bob@test.com"),
        )

        val family = Family(
            id = "f1",
            name = "Pinned Family",
            iconUrl = "https://example.com/family.png",
            members = members,
            memberCount = 10,
            isPinned = true,
        )

        assertEquals(members, family.members)
        assertEquals(10, family.memberCount)
        assertTrue(family.isPinned)
    }

    @Test
    fun `MemberPresence lastSeen defaults to current time`() {
        val before = System.currentTimeMillis()

        val presence = MemberPresence(
            userId = "u1",
            name = "Alice",
            email = "alice@test.com",
            lat = -6.89,
            lng = 107.61,
            rotation = 45f,
            battery = 85,
            charging = true,
            internetStatus = "wifi",
        )

        val after = System.currentTimeMillis()

        assertTrue(presence.lastSeen in before..after)
    }

    @Test
    fun `MemberPresence keeps explicit lastSeen value`() {
        val presence = MemberPresence(
            userId = "u1",
            name = "Alice",
            email = "alice@test.com",
            lat = -6.89,
            lng = 107.61,
            rotation = 45f,
            battery = 85,
            charging = true,
            internetStatus = "wifi",
            lastSeen = 123456789L,
        )

        assertEquals(123456789L, presence.lastSeen)
    }
}
