package app.learnalphabet.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemoCacheTest {

    @Test
    fun get_loadsOnce_thenServesFromMemory() {
        var calls = 0
        val cache = MemoCache<String, String> { calls++; "v-$it" }
        assertEquals("v-a", cache.get("a"))
        assertEquals("v-a", cache.get("a"))
        assertEquals(1, calls)
    }

    @Test
    fun peek_beforeLoad_isNull_afterLoad_isValue() {
        val cache = MemoCache<String, String> { "v-$it" }
        assertNull(cache.peek("a"))
        cache.get("a")
        assertEquals("v-a", cache.peek("a"))
    }

    @Test
    fun missingValue_isRemembered_notReloaded() {
        var calls = 0
        val cache = MemoCache<String, String> { calls++; null }
        assertNull(cache.get("x"))
        assertNull(cache.get("x"))
        assertEquals(1, calls)
        assertEquals(true, cache.isKnownMissing("x"))
    }
}
