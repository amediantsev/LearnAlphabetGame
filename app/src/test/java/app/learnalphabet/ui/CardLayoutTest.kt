package app.learnalphabet.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class CardLayoutTest {

    @Test
    fun letterScale_isLimitedByWidthWhenTextIsWide() =
        // 200 px wide at reference; 400 px available -> may double. Height allows x4.
        assertEquals(2f, letterScale(widest = 200f, lineHeight = 100f, maxWidth = 400f, maxHeight = 400f), 0.001f)

    @Test
    fun letterScale_isLimitedByHeightWhenSpaceIsShort() =
        assertEquals(1.5f, letterScale(widest = 200f, lineHeight = 100f, maxWidth = 1000f, maxHeight = 150f), 0.001f)

    @Test
    fun pictureSide_isHalfTheWidthOnTallScreens() =
        assertEquals(180f, pictureSide(maxWidth = 360f, maxHeight = 700f), 0.001f)

    @Test
    fun pictureSide_shrinksOnShortScreens() =
        assertEquals(96f, pictureSide(maxWidth = 360f, maxHeight = 300f), 0.001f)
}
