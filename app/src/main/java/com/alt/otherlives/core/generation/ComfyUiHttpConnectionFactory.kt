package com.alt.otherlives.core.generation

import java.net.HttpURLConnection
import java.net.URL

/**
 * Creates ComfyUI connections without following server-controlled redirects.
 *
 * A ComfyUI server receives personal photos and scenario prompts. A 3xx
 * response must not silently forward a subsequent request to another host.
 * The calling client handles redirects as unsuccessful HTTP responses.
 */
internal object ComfyUiHttpConnectionFactory {
    fun open(
        baseUrl: String,
        path: String,
        method: String,
        connectTimeoutMs: Int,
        readTimeoutMs: Int
    ): HttpURLConnection {
        require(path.startsWith("/") && !path.startsWith("//")) {
            "ComfyUI request path must be absolute and local to the configured endpoint"
        }
        val url = URL(baseUrl + path)
        require(url.protocol.equals("https", ignoreCase = true)) {
            "ComfyUI requests require HTTPS"
        }

        return (url.openConnection() as HttpURLConnection).apply {
            // Must be set before any request method, headers, or I/O.
            instanceFollowRedirects = false
            requestMethod = method
            connectTimeout = connectTimeoutMs
            readTimeout = readTimeoutMs
            useCaches = false
        }
    }
}
