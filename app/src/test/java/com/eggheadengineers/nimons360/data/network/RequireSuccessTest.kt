package com.eggheadengineers.nimons360.data.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Response

class RequireSuccessTest {

    // ── Success responses ──

    @Test
    fun `200 OK does not throw`() {
        Response.success(200, "body").requireSuccess("Default")
    }

    @Test
    fun `201 Created does not throw`() {
        Response.success(201, "body").requireSuccess("Default")
    }

    // ── Error responses with parseable message ──

    @Test
    fun `401 with error JSON throws parsed server message`() {
        val body = """{"error":{"code":"UNAUTHORIZED","message":"Invalid credentials"}}"""
            .toResponseBody(JSON)
        val ex = runCatching { Response.error<String>(401, body).requireSuccess("Login failed") }
            .exceptionOrNull()
        assertEquals("Invalid credentials", ex?.message)
    }

    @Test
    fun `404 with error JSON throws parsed server message`() {
        val body = """{"error":{"code":"NOT_FOUND","message":"Family not found"}}"""
            .toResponseBody(JSON)
        val ex = runCatching { Response.error<String>(404, body).requireSuccess("Default") }
            .exceptionOrNull()
        assertEquals("Family not found", ex?.message)
    }

    @Test
    fun `unknown extra fields in error body are ignored`() {
        val body = """{"error":{"code":"ERR","message":"Oops","extra":"ignored"},"requestId":"abc"}"""
            .toResponseBody(JSON)
        val ex = runCatching { Response.error<String>(400, body).requireSuccess("Default") }
            .exceptionOrNull()
        assertEquals("Oops", ex?.message)
    }

    // ── Error responses that fall back to default ──

    @Test
    fun `blank body uses default message with code`() {
        val body = "".toResponseBody(JSON)
        val ex = runCatching { Response.error<String>(500, body).requireSuccess("Server error") }
            .exceptionOrNull()
        assertEquals("Server error (500)", ex?.message)
    }

    @Test
    fun `malformed JSON body uses default message with code`() {
        val body = "{ not valid json at all".toResponseBody(JSON)
        val ex = runCatching { Response.error<String>(400, body).requireSuccess("Bad request") }
            .exceptionOrNull()
        assertEquals("Bad request (400)", ex?.message)
    }

    @Test
    fun `null error object uses default message`() {
        val body = """{"error":null}""".toResponseBody(JSON)
        val ex = runCatching { Response.error<String>(400, body).requireSuccess("Bad request") }
            .exceptionOrNull()
        assertEquals("Bad request (400)", ex?.message)
    }

    @Test
    fun `null error message field uses default message`() {
        val body = """{"error":{"code":"ERR","message":null}}""".toResponseBody(JSON)
        val ex = runCatching { Response.error<String>(400, body).requireSuccess("Bad request") }
            .exceptionOrNull()
        assertEquals("Bad request (400)", ex?.message)
    }

    @Test
    fun `blank error message field uses default message`() {
        val body = """{"error":{"code":"ERR","message":"   "}}""".toResponseBody(JSON)
        val ex = runCatching { Response.error<String>(400, body).requireSuccess("Bad request") }
            .exceptionOrNull()
        assertEquals("Bad request (400)", ex?.message)
    }

    // ── Error is always thrown on non-2xx ──

    @Test
    fun `any non-2xx response always throws`() {
        listOf(400, 401, 403, 404, 409, 422, 500, 503).forEach { code ->
            val body = "".toResponseBody(JSON)
            val result = runCatching { Response.error<String>(code, body).requireSuccess("Err") }
            assertTrue("Expected failure for HTTP $code", result.isFailure)
        }
    }

    companion object {
        private val JSON = "application/json".toMediaType()
    }
}
