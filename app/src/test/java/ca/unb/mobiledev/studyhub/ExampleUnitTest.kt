package ca.unb.mobiledev.studyhub

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {


    @Test
    fun formatDuration_zeroMs_is_00_00_00() {
        val result = TimeUtils.formatDurationMs(0L)
        assertEquals("00:00:00", result)
    }

    @Test
    fun formatDuration_underOneMinute() {
        // 45 seconds
        val result = TimeUtils.formatDurationMs(45_000L)
        assertEquals("00:00:45", result)
    }

    @Test
    fun formatDuration_oneHourOneMinuteOneSecond() {
        // 1h 1m 1s = 3600000 + 60000 + 1000 ms
        val ms = 3_600_000L + 60_000L + 1_000L
        val result = TimeUtils.formatDurationMs(ms)
        assertEquals("01:01:01", result)
    }

    @Test
    fun formatDuration_multipleHours() {
        // 3h 5m 9s
        val ms = (3 * 3_600_000L) + (5 * 60_000L) + (9 * 1_000L)
        val result = TimeUtils.formatDurationMs(ms)
        assertEquals("03:05:09", result)
    }

    // --- TimeUtils.calculateDeltaHours tests ---

    @Test
    fun calculateDeltaHours_noChange_returnsZero() {
        val lastSaved = 2.5   // hours
        val totalMs = (2.5 * 3_600_000).toLong()

        val delta = TimeUtils.calculateDeltaHours(lastSaved, totalMs)

        assertEquals(0.0, delta, 1e-6)
    }

    @Test
    fun calculateDeltaHours_increase_returnsPositiveDelta() {
        val lastSaved = 1.0   // 1 hour previously saved
        // total = 1h 30m
        val totalMs = (1.5 * 3_600_000).toLong()

        val delta = TimeUtils.calculateDeltaHours(lastSaved, totalMs)

        // Expect extra 0.5 hours
        assertEquals(0.5, delta, 1e-6)
    }

    @Test
    fun calculateDeltaHours_fromZero_equalsTotalHours() {
        val lastSaved = 0.0
        val totalMs = (0.75 * 3_600_000).toLong()

        val delta = TimeUtils.calculateDeltaHours(lastSaved, totalMs)

        assertEquals(0.75, delta, 1e-6)
    }
}
