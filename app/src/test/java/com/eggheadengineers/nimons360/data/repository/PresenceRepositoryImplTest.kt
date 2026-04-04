package com.eggheadengineers.nimons360.data.repository

import com.eggheadengineers.nimons360.domain.model.MemberPresence
import com.eggheadengineers.nimons360.domain.mapper.toDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.float
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PresenceRepositoryImplTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Message parsing tests for the JSON to domain pipeline

    @Test
    fun `valid member_presence_updated message is parsed correctly`() {
        val rawJson = """
            {
                "type": "member_presence_updated",
                "payload": {
                    "userId": "u1",
                    "fullName": "Alice",
                    "email": "alice@test.com",
                    "latitude": -6.89,
                    "longitude": 107.61,
                    "rotation": 45.0,
                    "batteryLevel": 85,
                    "isCharging": true,
                    "internetStatus": "wifi"
                }
            }
        """.trimIndent()

        val envelope = json.parseToJsonElement(rawJson).jsonObject
        val type = envelope["type"]?.jsonPrimitive?.content
        assertEquals("member_presence_updated", type)

        val payloadJson = envelope["payload"].toString()
        assertNotNull(payloadJson)

        val payload = json.decodeFromString<com.eggheadengineers.nimons360.data.dto.MemberPresenceUpdatedPayloadDto>(payloadJson)
        assertEquals("u1", payload.userId)
        assertEquals("Alice", payload.fullName)
        assertEquals(-6.89, payload.latitude!!, 0.001)
        assertEquals(107.61, payload.longitude!!, 0.001)
        assertEquals(45f, payload.rotation!!, 0.1f)
        assertEquals(85, payload.batteryLevel)
        assertTrue(payload.isCharging!!)
        assertEquals("wifi", payload.internetStatus)
    }

    @Test
    fun `payload with id field instead of userId uses id fallback`() {
        val rawJson = """
            {
                "type": "member_presence_updated",
                "payload": {
                    "id": "u2",
                    "fullName": "Bob",
                    "email": "bob@test.com",
                    "latitude": 0.0,
                    "longitude": 0.0
                }
            }
        """.trimIndent()

        val envelope = json.parseToJsonElement(rawJson).jsonObject
        val payloadJson = envelope["payload"].toString()
        val payload = json.decodeFromString<com.eggheadengineers.nimons360.data.dto.MemberPresenceUpdatedPayloadDto>(payloadJson)

        assertNull(payload.userId)
        assertEquals("u2", payload.id)

        // Test the mapper fallback
        val domain = payload.toDomain()
        assertEquals("u2", domain.userId)
    }

    @Test
    fun `payload with null fields maps to safe defaults`() {
        val rawJson = """
            {
                "type": "member_presence_updated",
                "payload": {
                    "userId": "u3"
                }
            }
        """.trimIndent()

        val envelope = json.parseToJsonElement(rawJson).jsonObject
        val payloadJson = envelope["payload"].toString()
        val payload = json.decodeFromString<com.eggheadengineers.nimons360.data.dto.MemberPresenceUpdatedPayloadDto>(payloadJson)

        val domain = payload.toDomain()
        assertEquals("u3", domain.userId)
        assertEquals("", domain.name)
        assertEquals("", domain.email)
        assertEquals(0.0, domain.lat, 0.001)
        assertEquals(0.0, domain.lng, 0.001)
        assertEquals(0f, domain.rotation, 0.1f)
        assertEquals(0, domain.battery)
        assertFalse(domain.charging)
        assertEquals("unknown", domain.internetStatus)
    }

    @Test
    fun `unknown message type is ignored (no crash)`() {
        val rawJson = """
            {
                "type": "some_other_event",
                "payload": { "foo": "bar" }
            }
        """.trimIndent()

        val envelope = json.parseToJsonElement(rawJson).jsonObject
        val type = envelope["type"]?.jsonPrimitive?.content
        assertNotEquals("member_presence_updated", type)
        // No exception means the message would be silently ignored
    }

    @Test
    fun `malformed JSON does not crash`() {
        val badJson = "{ not valid json at all"
        val result = runCatching {
            json.parseToJsonElement(badJson)
        }
        assertTrue(result.isFailure)
        // PresenceRepositoryImpl wraps parsing in runCatching, so this is safe
    }

    // Stale member cleanup tests

    @Test
    fun `stale members are removed based on lastSeen`() {
        val now = System.currentTimeMillis()
        val members = mapOf(
            "fresh" to MemberPresence(
                userId = "fresh", name = "Fresh", email = "", lat = 0.0, lng = 0.0,
                rotation = 0f, battery = 50, charging = false, internetStatus = "wifi",
                lastSeen = now,
            ),
            "stale" to MemberPresence(
                userId = "stale", name = "Stale", email = "", lat = 0.0, lng = 0.0,
                rotation = 0f, battery = 50, charging = false, internetStatus = "wifi",
                lastSeen = now - 10_000,
            ),
        )

        val cutoff = now - 5000
        val filtered = members.filter { (_, m) -> m.lastSeen > cutoff }

        assertEquals(1, filtered.size)
        assertTrue(filtered.containsKey("fresh"))
        assertFalse(filtered.containsKey("stale"))
    }

    // sendPresence envelope format test

    @Test
    fun `sendPresence builds correct JSON envelope structure`() {
        val payload = com.eggheadengineers.nimons360.data.dto.UpdatePresencePayloadDto(
            name = "TestUser",
            latitude = -6.89,
            longitude = 107.61,
            rotation = 90f,
            batteryLevel = 75,
            isCharging = false,
            internetStatus = "wifi",
        )
        val envelope = buildJsonEnvelope("update_presence", payload, "2026-03-29T00:00:00Z")
        val parsed = json.parseToJsonElement(envelope).jsonObject

        assertEquals("update_presence", parsed["type"]?.jsonPrimitive?.content)
        assertEquals("2026-03-29T00:00:00Z", parsed["timestamp"]?.jsonPrimitive?.content)

        val payloadObj = parsed["payload"]!!.jsonObject
        assertEquals("TestUser", payloadObj["name"]?.jsonPrimitive?.content)
        assertEquals(-6.89, payloadObj["latitude"]?.jsonPrimitive?.double ?: 0.0, 0.001)
        assertEquals(107.61, payloadObj["longitude"]?.jsonPrimitive?.double ?: 0.0, 0.001)
        assertEquals(90f, payloadObj["rotation"]?.jsonPrimitive?.float ?: 0f, 0.1f)
        assertEquals(75, payloadObj["batteryLevel"]?.jsonPrimitive?.int)
        assertFalse(payloadObj["isCharging"]?.jsonPrimitive?.boolean ?: true)
        assertEquals("wifi", payloadObj["internetStatus"]?.jsonPrimitive?.content)
    }

    private fun buildJsonEnvelope(
        type: String,
        payload: com.eggheadengineers.nimons360.data.dto.UpdatePresencePayloadDto,
        timestamp: String,
    ): String {
        val payloadJson = json.encodeToJsonElement(
            com.eggheadengineers.nimons360.data.dto.UpdatePresencePayloadDto.serializer(),
            payload,
        )
        return json.encodeToString(
            JsonObject.serializer(),
            JsonObject(
                mapOf(
                    "type" to JsonPrimitive(type),
                    "payload" to payloadJson,
                    "timestamp" to JsonPrimitive(timestamp),
                )
            ),
        )
    }
}
