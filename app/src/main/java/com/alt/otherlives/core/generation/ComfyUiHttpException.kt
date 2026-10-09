package com.alt.otherlives.core.generation

/**
 * A failed ComfyUI HTTP response with an actual transport status code.
 *
 * Only these typed errors should be considered for HTTP status-based retries:
 * remote error messages and workflow exceptions must never impersonate
 * an HTTP 429 or 5xx by containing a matching text fragment.
 */
internal class ComfyUiHttpException(
    val statusCode: Int,
    responseSummary: String = ""
) : IllegalStateException(
    "ComfyUI HTTP " + statusCode +
        responseSummary.takeIf { it.isNotBlank() }?.let { ": " + it }.orEmpty()
)
