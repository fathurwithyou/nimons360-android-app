package com.eggheadengineers.nimons360.data.network

import org.junit.Assert.*
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class ApiErrorTest {

    @Test
    fun `ConnectException returns connection message`() {
        val ex = ConnectException("Failed to connect to /10.0.2.2:3000")
        assertEquals(
            "Unable to reach the server. Please check your internet connection and try again.",
            ex.userFriendlyMessage("fallback"),
        )
    }

    @Test
    fun `UnknownHostException returns connection message`() {
        val ex = UnknownHostException("Unable to resolve host \"10.0.2.2\"")
        assertEquals(
            "Unable to reach the server. Please check your internet connection and try again.",
            ex.userFriendlyMessage("fallback"),
        )
    }

    @Test
    fun `SocketTimeoutException returns timeout message`() {
        val ex = SocketTimeoutException("timeout")
        assertEquals(
            "The request timed out. Please check your connection and try again.",
            ex.userFriendlyMessage("fallback"),
        )
    }

    @Test
    fun `SSLException returns secure connection message`() {
        val ex = SSLException("SSL handshake aborted")
        assertEquals(
            "A secure connection could not be established. Please try again later.",
            ex.userFriendlyMessage("fallback"),
        )
    }

    @Test
    fun `generic exception with 'failed to connect' in message returns connection message`() {
        val ex = RuntimeException("failed to connect to /10.0.2.2:3000 after 10000ms")
        assertEquals(
            "Unable to reach the server. Please check your internet connection and try again.",
            ex.userFriendlyMessage("fallback"),
        )
    }

    @Test
    fun `generic exception with 'timeout' in message returns timeout message`() {
        val ex = RuntimeException("Read timeout after 30 seconds")
        assertEquals(
            "The request timed out. Please check your connection and try again.",
            ex.userFriendlyMessage("fallback"),
        )
    }

    @Test
    fun `generic exception with non-network message preserves original message`() {
        val ex = RuntimeException("Something unexpected")
        assertEquals("Something unexpected", ex.userFriendlyMessage("fallback"))
    }

    @Test
    fun `exception with null message returns fallback`() {
        val ex = RuntimeException(null as String?)
        assertEquals("fallback", ex.userFriendlyMessage("fallback"))
    }

    @Test
    fun `API error message is preserved as-is`() {
        val ex = IllegalStateException("Invalid email or password")
        assertEquals("Invalid email or password", ex.userFriendlyMessage("Login failed"))
    }
}
