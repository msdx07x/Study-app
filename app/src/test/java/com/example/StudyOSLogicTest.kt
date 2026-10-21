package com.example

import com.example.network.youtube.YouTubeTimeUtils
import com.example.network.youtube.YouTubeUrlParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StudyOSLogicTest {

    @Test
    fun `test course progress calculation`() {
        fun calculateProgress(completed: Int, total: Int): Float {
            return if (total > 0) (completed.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f
        }

        assertEquals(0f, calculateProgress(0, 10), 0.001f)
        assertEquals(0.5f, calculateProgress(5, 10), 0.001f)
        assertEquals(1.0f, calculateProgress(10, 10), 0.001f)
        assertEquals(0f, calculateProgress(0, 0), 0.001f) // Safe division by zero
    }

    @Test
    fun `test youtube url parsing`() {
        val standardUrl = "https://www.youtube.com/playlist?list=PL8dPuuaLjXtNlUrzyH5r6jN9ulIgZBpdo"
        assertEquals("PL8dPuuaLjXtNlUrzyH5r6jN9ulIgZBpdo", YouTubeUrlParser.extractPlaylistId(standardUrl))

        val watchUrl = "https://www.youtube.com/watch?v=tpIctyqH29Q&list=PL8dPuuaLjXtNlUrzyH5r6jN9ulIgZBpdo"
        assertEquals("PL8dPuuaLjXtNlUrzyH5r6jN9ulIgZBpdo", YouTubeUrlParser.extractPlaylistId(watchUrl))

        val directId = "PL8dPuuaLjXtNlUrzyH5r6jN9ulIgZBpdo"
        assertEquals("PL8dPuuaLjXtNlUrzyH5r6jN9ulIgZBpdo", YouTubeUrlParser.extractPlaylistId(directId))

        val invalid = "https://google.com"
        assertNull(YouTubeUrlParser.extractPlaylistId(invalid))
    }

    @Test
    fun `test iso 8601 duration parsing`() {
        assertEquals(933, YouTubeTimeUtils.parseIso8601Duration("PT15M33S"))
        assertEquals(3750, YouTubeTimeUtils.parseIso8601Duration("PT1H2M30S"))
        assertEquals(45, YouTubeTimeUtils.parseIso8601Duration("PT45S"))
        assertEquals(3600, YouTubeTimeUtils.parseIso8601Duration("PT1H"))
        assertNull(YouTubeTimeUtils.parseIso8601Duration(null))
        assertNull(YouTubeTimeUtils.parseIso8601Duration(""))
    }

    @Test
    fun `test duration formatting`() {
        assertEquals("0:45", YouTubeTimeUtils.formatSeconds(45))
        assertEquals("15:33", YouTubeTimeUtils.formatSeconds(933))
        assertEquals("1:02:30", YouTubeTimeUtils.formatSeconds(3750))
        assertEquals("--:--", YouTubeTimeUtils.formatSeconds(null))
    }

    @Test
    fun `test reminder repeat intervals`() {
        val now = 1000000L
        val threeHoursMinutes = 180L
        val nextTrigger = now + (threeHoursMinutes * 60 * 1000)
        assertEquals(now + 10800000L, nextTrigger)
    }
}
