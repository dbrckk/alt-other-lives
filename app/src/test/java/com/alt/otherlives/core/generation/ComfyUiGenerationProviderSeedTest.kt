package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ComfyUiGenerationProviderSeedTest {
    @Test
    fun preservesExplicitPositiveSeed() {
        assertEquals(
            123456789L,
            ComfyUiGenerationProvider.resolveSessionSeed(123456789L)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsZeroExplicitSeed() {
        ComfyUiGenerationProvider.resolveSessionSeed(0L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsNegativeExplicitSeed() {
        ComfyUiGenerationProvider.resolveSessionSeed(-1L)
    }

    @Test
    fun missingSeedGeneratesPositiveValue() {
        repeat(32) {
            assertTrue(ComfyUiGenerationProvider.resolveSessionSeed(null) > 0L)
        }
    }
}
