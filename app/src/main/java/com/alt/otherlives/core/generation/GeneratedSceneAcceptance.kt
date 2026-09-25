package com.alt.otherlives.core.generation

internal object GeneratedSceneAcceptance {
    suspend fun <T> accept(
        scene: T,
        onSceneGenerated: suspend (T) -> Unit,
        accepted: MutableList<T>
    ) {
        onSceneGenerated(scene)
        accepted += scene
    }
}
