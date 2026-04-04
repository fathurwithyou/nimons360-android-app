package com.eggheadengineers.nimons360.data.dto

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.*
import org.junit.Test

class DtoSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `LoginRequestDto serializes email and password`() {
        val dto = LoginRequestDto(email = "alice@stei.itb.ac.id", password = "secret")
        val encoded = json.encodeToString(LoginRequestDto.serializer(), dto)
        assertTrue(encoded.contains("\"email\":\"alice@stei.itb.ac.id\""))
        assertTrue(encoded.contains("\"password\":\"secret\""))
    }

    @Test
    fun `LoginApiResponse deserializes full response`() {
        val raw = """
            {
                "data": {
                    "token": "abc123",
                    "expiresAt": "2026-12-31T00:00:00Z",
                    "user": {
                        "id": 1,
                        "nim": "13521099",
                        "email": "alice@stei.itb.ac.id",
                        "fullName": "Alice"
                    }
                }
            }
        """.trimIndent()

        val result = json.decodeFromString<LoginApiResponse>(raw)
        assertEquals("abc123", result.data?.token)
        assertEquals(1, result.data?.user?.id)
        assertEquals("Alice", result.data?.user?.fullName)
        assertEquals("13521099", result.data?.user?.nim)
        assertEquals("2026-12-31T00:00:00Z", result.data?.expiresAt)
    }

    @Test
    fun `LoginApiResponse handles null data`() {
        val result = json.decodeFromString<LoginApiResponse>("""{"data":null}""")
        assertNull(result.data)
    }

    @Test
    fun `LoginApiResponse handles null token and user`() {
        val raw = """{"data":{"token":null,"expiresAt":null,"user":null}}"""
        val result = json.decodeFromString<LoginApiResponse>(raw)
        assertNull(result.data?.token)
        assertNull(result.data?.user)
    }

    @Test
    fun `ProfileApiResponse deserializes with all optional nulls`() {
        val raw = """{"data":{"id":1,"nim":"13521099","email":"a@b.com","fullName":"Alice","createdAt":null,"updatedAt":null}}"""
        val result = json.decodeFromString<ProfileApiResponse>(raw)
        assertEquals(1, result.data?.id)
        assertEquals("13521099", result.data?.nim)
        assertNull(result.data?.createdAt)
        assertNull(result.data?.updatedAt)
    }

    @Test
    fun `UpdateProfileRequestDto serializes fullName`() {
        val dto = UpdateProfileRequestDto(fullName = "Alice Updated")
        val encoded = json.encodeToString(UpdateProfileRequestDto.serializer(), dto)
        assertTrue(encoded.contains("\"fullName\":\"Alice Updated\""))
    }

    // Family DTOs

    @Test
    fun `FamilyListApiResponse deserializes members list`() {
        val raw = """
            {
                "data": [{
                    "id": 1,
                    "name": "Family A",
                    "iconUrl": "https://example.com/icon.png",
                    "memberCount": 2,
                    "familyCode": null,
                    "createdAt": null,
                    "updatedAt": null,
                    "members": [
                        {"id": 10, "fullName": "Alice", "email": "a@t.com", "joinedAt": null},
                        {"id": 11, "fullName": "Bob", "email": "b@t.com", "joinedAt": null}
                    ]
                }]
            }
        """.trimIndent()

        val result = json.decodeFromString<FamilyListApiResponse>(raw)
        assertEquals(1, result.data?.size)
        assertEquals("Family A", result.data?.first()?.name)
        assertEquals(2, result.data?.first()?.members?.size)
        assertEquals("Alice", result.data?.first()?.members?.first()?.fullName)
    }

    @Test
    fun `FamilySummaryDto handles null optional fields`() {
        val raw = """{"data":[{"id":1,"name":"F","iconUrl":"url","memberCount":null,"familyCode":null,"createdAt":null,"updatedAt":null,"members":null}]}"""
        val result = json.decodeFromString<FamilyListApiResponse>(raw)
        assertNull(result.data?.first()?.members)
        assertNull(result.data?.first()?.familyCode)
        assertNull(result.data?.first()?.memberCount)
    }

    @Test
    fun `FamilyDetailApiResponse deserializes with isMember and familyCode`() {
        val raw = """
            {
                "data": {
                    "id": 5,
                    "name": "Test Family",
                    "iconUrl": "https://example.com/icon.png",
                    "familyCode": "ABCD12",
                    "isMember": true,
                    "createdAt": null,
                    "updatedAt": null,
                    "members": []
                }
            }
        """.trimIndent()

        val result = json.decodeFromString<FamilyDetailApiResponse>(raw)
        assertEquals(5, result.data?.id)
        assertEquals("ABCD12", result.data?.familyCode)
        assertTrue(result.data?.isMember ?: false)
        assertEquals(0, result.data?.members?.size)
    }

    @Test
    fun `CreateFamilyRequestDto serializes name and iconUrl`() {
        val dto = CreateFamilyRequestDto(name = "My Family", iconUrl = "https://img.url/icon.png")
        val encoded = json.encodeToString(CreateFamilyRequestDto.serializer(), dto)
        assertTrue(encoded.contains("\"name\":\"My Family\""))
        assertTrue(encoded.contains("\"iconUrl\":\"https://img.url/icon.png\""))
    }

    @Test
    fun `JoinFamilyRequestDto serializes familyId and familyCode`() {
        val dto = JoinFamilyRequestDto(familyId = 7, familyCode = "CODE99")
        val encoded = json.encodeToString(JoinFamilyRequestDto.serializer(), dto)
        assertTrue(encoded.contains("\"familyId\":7"))
        assertTrue(encoded.contains("\"familyCode\":\"CODE99\""))
    }

    // WebSocket DTOs

    @Test
    fun `WsEnvelopeDto deserializes type and timestamp`() {
        val raw = """{"type":"member_presence_updated","payload":{"userId":"u1"},"timestamp":"2026-04-05T00:00:00Z"}"""
        val dto = json.decodeFromString<WsEnvelopeDto>(raw)
        assertEquals("member_presence_updated", dto.type)
        assertNotNull(dto.payload)
        assertEquals("2026-04-05T00:00:00Z", dto.timestamp)
    }

    @Test
    fun `WsEnvelopeDto handles null payload`() {
        val raw = """{"type":"ping","payload":null}"""
        val dto = json.decodeFromString<WsEnvelopeDto>(raw)
        assertEquals("ping", dto.type)
        assertNull(dto.payload)
    }

    @Test
    fun `WsEnvelopeDto handles missing timestamp`() {
        val raw = """{"type":"pong","payload":null}"""
        val dto = json.decodeFromString<WsEnvelopeDto>(raw)
        assertNull(dto.timestamp)
    }

    @Test
    fun `UpdatePresencePayloadDto serializes all fields correctly`() {
        val dto = UpdatePresencePayloadDto(
            name = "Alice",
            latitude = -6.891,
            longitude = 107.612,
            rotation = 90f,
            batteryLevel = 80,
            isCharging = false,
            internetStatus = "wifi",
        )
        val encoded = json.encodeToString(UpdatePresencePayloadDto.serializer(), dto)
        assertTrue(encoded.contains("\"name\":\"Alice\""))
        assertTrue(encoded.contains("\"batteryLevel\":80"))
        assertTrue(encoded.contains("\"isCharging\":false"))
        assertTrue(encoded.contains("\"internetStatus\":\"wifi\""))
        assertTrue(encoded.contains("\"metadata\":{}"))
    }

    @Test
    fun `MemberPresenceUpdatedPayloadDto deserializes with all optional fields`() {
        val raw = """
            {
                "userId": "u1",
                "id": null,
                "fullName": "Alice",
                "email": "alice@test.com",
                "latitude": -6.891,
                "longitude": 107.612,
                "rotation": 180.0,
                "batteryLevel": 92,
                "isCharging": true,
                "internetStatus": "wifi"
            }
        """.trimIndent()

        val dto = json.decodeFromString<MemberPresenceUpdatedPayloadDto>(raw)
        assertEquals("u1", dto.userId)
        assertNull(dto.id)
        assertEquals(-6.891, dto.latitude!!, 0.0001)
        assertEquals(107.612, dto.longitude!!, 0.0001)
        assertEquals(180f, dto.rotation!!, 0.01f)
        assertEquals(92, dto.batteryLevel)
        assertTrue(dto.isCharging!!)
    }

    @Test
    fun `MemberPresenceUpdatedPayloadDto deserializes with only userId present`() {
        val dto = json.decodeFromString<MemberPresenceUpdatedPayloadDto>("""{"userId":"u3"}""")
        assertEquals("u3", dto.userId)
        assertNull(dto.latitude)
        assertNull(dto.batteryLevel)
        assertNull(dto.isCharging)
        assertNull(dto.metadata)
    }

    @Test
    fun `MemberPresenceUpdatedPayloadDto falls back to id field`() {
        val dto = json.decodeFromString<MemberPresenceUpdatedPayloadDto>("""{"id":"fallback-id"}""")
        assertNull(dto.userId)
        assertEquals("fallback-id", dto.id)
    }

    @Test
    fun `unknown fields are silently ignored`() {
        val raw = """
            {
                "id": 1,
                "nim": "13521099",
                "email": "a@b.com",
                "fullName": "Alice",
                "createdAt": null,
                "updatedAt": null,
                "newFieldFromFutureApiVersion": "ignored"
            }
        """.trimIndent()
        // Should not throw
        val dto = json.decodeFromString<ProfileDto>(raw)
        assertEquals("Alice", dto.fullName)
    }

    // FamilyMemberDto

    @Test
    fun `FamilyMemberDto handles null id`() {
        val raw = """{"id":null,"fullName":"Guest","email":"guest@test.com","joinedAt":null}"""
        val dto = json.decodeFromString<FamilyMemberDto>(raw)
        assertNull(dto.id)
        assertEquals("Guest", dto.fullName)
    }
}
