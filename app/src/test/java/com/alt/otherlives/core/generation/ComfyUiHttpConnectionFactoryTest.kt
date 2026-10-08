package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ComfyUiHttpConnectionFactoryTest {
    @Test
    fun imageUploadDoesNotFollowServerRedirects() {
        val connection = ComfyUiHttpConnectionFactory.open(
            baseUrl = "https://comfy.example/proxy",
            path = "/upload/image",
            method = "POST",
            connectTimeoutMs = 15_000,
            readTimeoutMs = 60_000
        )
        try {
            // Construction is lazy: no network call is needed for this test.
            assertFalse(connection.instanceFollowRedirects)
            assertEquals("POST", connection.requestMethod)
            assertEquals("https://comfy.example/proxy/upload/image", connection.url.toString())
            assertEquals(15_000, connection.connectTimeout)
            assertEquals(60_000, connection.readTimeout)
            assertFalse(connection.useCaches)
        } finally {
            connection.disconnect()
        }
    }

    @Test
    fun healthChecksAlsoRejectRedirects() {
        val connection = ComfyUiHttpConnectionFactory.open(
            baseUrl = "https://comfy.example",
            path = "/system_stats",
            method = "GET",
            connectTimeoutMs = 8_000,
            readTimeoutMs = 10_000
        )
        try {
            assertFalse(connection.instanceFollowRedirects)
            assertEquals("GET", connection.requestMethod)
        } finally {
            connection.disconnect()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsPlainHttp() {
        ComfyUiHttpConnectionFactory.open(
            baseUrl = "http://comfy.example",
            path = "/prompt",
            method = "POST",
            connectTimeoutMs = 10_000,
            readTimeoutMs = 30_000
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNetworkPathReference() {
        ComfyUiHttpConnectionFactory.open(
            baseUrl = "https://comfy.example",
            path = "//other.example/path",
            method = "POST",
            connectTimeoutMs = 10_000,
            readTimeoutMs = 30_000
        )
    }
}
