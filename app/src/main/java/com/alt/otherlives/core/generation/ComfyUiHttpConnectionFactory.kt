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
    internal const val UPLOAD_CHUNK_SIZE_BYTES = 64 * 1024

    /**
     * HttpURLConnection otherwise buffers a POST request body in memory before
     * sending it. Source photos can be large, so stream multipart bytes using
     * bounded chunks instead of buffering the entire selected photo.
     *
     * Must be called before opening the request output stream.
     */
    fun configureMultipartUpload(connection: HttpURLConnection, boundary: String) {
        require(boundary.matches(Regex("[A-Za-z0-9-]{1,70}"))) {
            "Invalid multipart boundary"
        }
        connection.setRequestProperty(
            "Content-Type",
            "multipart/form-data; boundary=$boundary"
        )
        connection.doOutput = true
        connection.setChunkedStreamingMode(UPLOAD_CHUNK_SIZE_BYTES)
    }

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
