package com.alt.otherlives.app

import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.alt.otherlives.MainActivity
import com.alt.otherlives.R
import com.alt.otherlives.core.data.HistoryRepository
import com.alt.otherlives.core.data.ScenarioCatalog
import com.alt.otherlives.core.data.SourcePhotoStore
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Runs the actual MainActivity, DataStore and private photo FileProvider.
 * Tests restoring an existing timeline through History, rotating/recreating
 * the Activity in Reveal, and keeping its share affordance available.
 *
 * No mocked navigation, backend, generated imagery or ComfyUI server.
 */
class AltAppHistoryRestoreInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun savedPhotoTimelineOpensAndSurvivesActivityRecreation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val historyRepository = HistoryRepository(context)
        val sourcePhotoStore = SourcePhotoStore(context)
        val sourceDir = File(context.cacheDir, "shares").apply {
            assertTrue(isDirectory || mkdirs())
        }
        val source = File(sourceDir, "alt-restore-smoke-${UUID.randomUUID()}.jpg")
        val bitmap = Bitmap.createBitmap(768, 1152, Bitmap.Config.ARGB_8888)
        val scenario = ScenarioCatalog.forLanguage(
            context.resources.configuration.locales[0].language
        ).first()
        var importedFileName: String? = null

        try {
            bitmap.eraseColor(0xFF5E476C.toInt())
            source.outputStream().use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 91, output))
            }
            val sourceUri = FileProvider.getUriForFile(
                context,
                context.packageName + ".fileprovider",
                source
            )
            val imported = sourcePhotoStore.import(sourceUri)
            importedFileName = imported.fileName
            runBlocking {
                historyRepository.record(
                    scenarioId = scenario.id,
                    photoFileName = imported.fileName
                )
            }

            composeRule.onNodeWithText(context.getString(R.string.home_view_history))
                .performScrollTo()
                .performClick()
            composeRule.waitUntil(timeoutMillis = 15_000) {
                composeRule.onAllNodesWithText(context.getString(R.string.history_open))
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText(context.getString(R.string.history_open))
                .performClick()
            composeRule.waitUntil(timeoutMillis = 15_000) {
                composeRule.onAllNodesWithText(scenario.title)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText(scenario.title).assertIsDisplayed()

            // Simulates an Android Activity recreation with SavedState while
            // the user is reading Reveal. The photo must be re-resolved from
            // private storage rather than relying on a stale in-memory URI.
            composeRule.activityRule.scenario.recreate()
            composeRule.waitUntil(timeoutMillis = 15_000) {
                composeRule.onAllNodesWithText(context.getString(R.string.reveal_share_life))
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText(scenario.title).assertIsDisplayed()
            composeRule.onNodeWithText(context.getString(R.string.reveal_share_life))
                .performScrollTo()
                .assertIsEnabled()
        } finally {
            bitmap.recycle()
            runBlocking { historyRepository.clear() }
            importedFileName?.let { fileName ->
                sourcePhotoStore.discardCancelledImport(fileName)
            }
            source.delete()
        }
    }
}
