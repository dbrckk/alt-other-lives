package com.alt.otherlives.core.media

import androidx.test.platform.app.InstrumentationRegistry
import com.alt.otherlives.core.data.ScenarioCatalog
import java.io.File
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Exercises the real Android bitmap encoder and app-private cinematic cache.
 * Cancelling after two completed chapter JPEGs must clean up both the newly
 * finalized JPEG and the previously completed chapter.
 */
class TimelineSceneRendererCancellationInstrumentedTest {
    @Test
    fun cancellationAfterMultipleEncodedChaptersLeavesNoNewCachedFrames() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val scenario = ScenarioCatalog.scenarios.first()
        val cache = File(context.cacheDir, "shares/scenes").apply {
            assertTrue(isDirectory || mkdirs())
        }
        val namesBefore = cache.listFiles().orEmpty().map { it.name }.toSet()
        var encodedChaptersSeen = 0

        try {
            val failure = runCatching {
                TimelineSceneRenderer.render(
                    context = context,
                    photoUri = null,
                    scenario = scenario,
                    checkCancelled = {
                        val newJpegs = cache.listFiles().orEmpty().count {
                            it.isFile && it.name !in namesBefore &&
                                it.name.startsWith("alt-${scenario.id}-scene-") &&
                                it.extension.equals("jpg", ignoreCase = true) &&
                                it.length() > 0L
                        }
                        if (newJpegs >= 2) {
                            encodedChaptersSeen = newJpegs
                            throw CancellationException("cancel after two chapter encodes")
                        }
                    }
                )
            }.exceptionOrNull()

            assertTrue("Expected cancellation during real image preparation", failure is CancellationException)
            assertTrue("Two chapter images must have been finalized", encodedChaptersSeen >= 2)
            assertTrue(
                "Cancelled video preparation must delete its own JPEGs and temporary files",
                cache.listFiles().orEmpty().none { it.name !in namesBefore }
            )
        } finally {
            cache.listFiles().orEmpty()
                .filter { it.name !in namesBefore }
                .forEach { it.delete() }
        }
    }
}
