package com.tinixmusic.tinixmusic2





import org.junit.Assert.assertEquals
import org.junit.Test

class MusicTypesTest {

    @Test
    fun getPersianLabel_returnsCorrectLabel() {
        assertEquals("آهسته", MusicTypes.getPersianLabel("Slowed"))
        assertEquals("ریمیکس", MusicTypes.getPersianLabel("Remix"))
        assertEquals("نایت‌کور", MusicTypes.getPersianLabel("Nightcore"))
    }

    @Test
    fun getPersianLabel_returnsInputIfUnknown() {
        assertEquals("Unknown", MusicTypes.getPersianLabel("Unknown"))
    }
}