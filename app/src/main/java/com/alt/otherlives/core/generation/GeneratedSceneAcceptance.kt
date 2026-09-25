package com.alt.otherlives.core.generation

internal object GeneratedSceneAcceptance {
    suspend fun accept(
        scene: GeneratedScene,
        onSceneGenerated: suspend (GeneratedScene) -> Unit,
        accepted: MutableList<GeneratedScene>
    ) {
        onSceneGenerated(scene)
        accepted += scene
    }
}
