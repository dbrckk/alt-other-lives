package com.alt.otherlives.core.generation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratedSceneSeedTest {
    @Test
    fun seedConversionAlwaysProducesPositiveValue() {
        assertEquals(1L, GeneratedSceneStore.positiveSeedFrom(0L))
        assertEquals(1L, GeneratedSceneStore.positiveSeedFrom(Long.MIN_VALUE))
        assertEquals(Long.MAX_VALUE, GeneratedSceneStore.positiveSeedFrom(-1L))
        assertEquals(42L, GeneratedSceneStore.positiveSeedFrom(42L))
    }

    @Test
    fun generatedSeedsStayWithinComfyUiPositiveRange() {
        val storeClass = GeneratedSceneStore::class.java
        val method = storeClass.getDeclaredMethod("positiveSeedFrom", Long::class.javaPrimitiveType)

        listOf(
            Long.MIN_VALUE,
            -123456789L,
            -1L,
            0L,
            1L,
            123456789L,
            Long.MAX_VALUE
        ).forEach { raw ->
            val seed = method.invoke(null, raw) as Long
            assertTrue(seed > 0L)
            assertTrue(seed <= Long.MAX_VALUE)
        }
    }
}
