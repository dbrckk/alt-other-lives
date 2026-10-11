package com.alt.otherlives.core.media

import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.alt.otherlives.core.data.ScenarioCatalog
import java.io.File
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates actual Android bitmap encoding and private FileProvider JPEGs.
 * A cancelled social export must not leave its newly encoded temp file.
 */
class ShareCardExportInstrumentedTest {
    @Test
    fun completedSocialJpegIsReadableAndExactlyNineBySixteen() = withShareCache {
        context, cache, previous ->
        val scenario = ScenarioCatalog.scenarios.first()
        val uri = ShareCardRenderer.render(
            context = context,
            photoUri = null,
            scenario = scenario
        )
        assertEquals("image/jpeg", context.contentResolver.getType(uri))
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)!!.use { input ->
            BitmapFactory.decodeStream(input, null, bounds)
        }
        assertEquals(1080, bounds.outWidth)
        assertEquals(1920, bounds.outHeight)
        assertTrue(
            "Social card should contain a nonempty JPEG in its private cache",
            cache.listFiles().orEmpty().any {
                it.name !in previous && it.extension == "jpg" && it.length() > 0L
            }
        )
    }

    @Test
    fun cancellationAfterJpegEncodingRemovesShareTempFile() = withShareCache {
        context, cache, previous ->
        val scenario = ScenarioCatalog.scenarios.first()
        var sawEncodedTemporaryFile = false
        val failure = runCatching {
            ShareCardRenderer.render(
                context = context,
                photoUri = null,
                scenario = scenario,
                checkCancelled = {
                    if (cache.listFiles().orEmpty().any {
                            it.name !in previous && it.isFile &&
                                it.name.startsWith(".alt-${scenario.id}-") &&
                                it.name.endsWith(".tmp") && it.length() > 0L
                        }) {
                        sawEncodedTemporaryFile = true
                        throw CancellationException("cancel after share JPEG encoding")
                    }
                }
            )
        }.exceptionOrNull()

        assertTrue("Cancellation must happen after a real JPEG encode", sawEncodedTemporaryFile)
        assertTrue("Expected coroutine-style cancellation", failure is CancellationException)
        assertTrue(
            "Cancelled social card must not leave new cache files",
            cache.listFiles().orEmpty().none { it.name !in previous }
        )
    }

    @Test
    fun cancellationAfterComparisonEncodingRemovesTempFile() = withShareCache {
        context, cache, previous ->
        val scenario = ScenarioCatalog.scenarios.first()
        var sawEncodedTemporaryFile = false
        val failure = runCatching {
            ShareCardRenderer.renderComparison(
                context = context,
                firstVisualUri = null,
                firstScenario = scenario,
                secondVisualUri = null,
                secondScenario = ScenarioCatalog.scenarios.last(),
                checkCancelled = {
                    if (cache.listFiles().orEmpty().any {
                            it.name !in previous && it.isFile &&
                                it.name.startsWith(".alt-compare-") &&
                                it.name.endsWith(".tmp") && it.length() > 0L
                        }) {
                        sawEncodedTemporaryFile = true
                        throw CancellationException("cancel after comparison JPEG encoding")
                    }
                }
            )
        }.exceptionOrNull()

        assertTrue("Cancellation must happen after a real JPEG encode", sawEncodedTemporaryFile)
        assertTrue("Expected coroutine-style cancellation", failure is CancellationException)
        assertTrue(
            "Cancelled comparison card must not leave new cache files",
            cache.listFiles().orEmpty().none { it.name !in previous }
        )
    }

    private inline fun withShareCache(
        block: (android.content.Context, File, Set<String>) -> Unit
    ) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val cache = File(context.cacheDir, "shares").apply {
            assertTrue(isDirectory || mkdirs())
        }
        val previous = cache.listFiles().orEmpty().map { it.name }.toSet()
        try {
            block(context, cache, previous)
        } finally {
            cache.listFiles().orEmpty()
                .filter { it.name !in previous && it.isFile }
                .forEach { it.delete() }
        }
    }
}
