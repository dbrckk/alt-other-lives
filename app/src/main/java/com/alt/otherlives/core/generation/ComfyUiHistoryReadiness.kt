package com.alt.otherlives.core.generation

/**
 * An output entry in history may represent an intermediate preview or a
 * partially completed multi-output workflow. Accept it only once the
 * server reports that the prompt has finished executing.
 */
internal object ComfyUiHistoryReadiness {
    enum class Decision {
        KEEP_POLLING,
        READY,
        COMPLETED_WITHOUT_IMAGES
    }

    fun decide(completed: Boolean, imageCount: Int): Decision {
        require(imageCount >= 0) { "ComfyUI history image count cannot be negative" }
        if (!completed) return Decision.KEEP_POLLING
        return if (imageCount > 0) Decision.READY else Decision.COMPLETED_WITHOUT_IMAGES
    }
}
