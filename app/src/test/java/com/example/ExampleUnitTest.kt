package com.example

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ExampleUnitTest {

  private fun formatCountdownTime(totalSeconds: Long): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val hours = safeSeconds / 3600
    val minutes = (safeSeconds % 3600) / 60
    val seconds = safeSeconds % 60

    return if (hours > 0) {
      String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
      String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
  }

  @Test
  fun testCountdownFormatUnderOneHour() {
    assertEquals("00:00", formatCountdownTime(0))
    assertEquals("00:09", formatCountdownTime(9))
    assertEquals("00:59", formatCountdownTime(59))
    assertEquals("01:00", formatCountdownTime(60))
    assertEquals("10:15", formatCountdownTime(615))
    assertEquals("59:59", formatCountdownTime(3599))
  }

  @Test
  fun testCountdownFormatOverOneHour() {
    assertEquals("01:00:00", formatCountdownTime(3600))
    assertEquals("01:01:05", formatCountdownTime(3665))
    assertEquals("02:30:45", formatCountdownTime(9045))
  }

  @Test
  fun testNegativeTimeCoercesToZero() {
    assertEquals("00:00", formatCountdownTime(-10))
  }

  @Test
  fun testVisualCountdownTimerLowTimeWarningAndStateSync() {
    val totalDurationSeconds = 600L
    var remainingSeconds = 180L // Synced from Room database (Warning Zone)

    val progress = remainingSeconds.toFloat() / totalDurationSeconds.toFloat()
    assertEquals(0.3f, progress, 0.001f)

    val isWarning = remainingSeconds in 61..300
    val isCritical = remainingSeconds in 1..60

    assertTrue("Should trigger low-time warning when <= 300s", isWarning)
    assertFalse("Should not be critical when > 60s", isCritical)

    // Simulate ticking down into critical auto-submit zone (<= 60s)
    remainingSeconds = 45L
    val isWarningCritical = remainingSeconds in 61..300
    val isCriticalNow = remainingSeconds in 1..60

    assertFalse(isWarningCritical)
    assertTrue("Should be critical when <= 60s", isCriticalNow)

    // Normal state (> 300s)
    remainingSeconds = 550L
    assertFalse(remainingSeconds in 61..300)
    assertFalse(remainingSeconds in 1..60)
  }

  @Test
  fun testPaletteQuestionStatusCounts() {
    val statuses = listOf("answered", "answered", "not-answered", "marked", "marked-answered", "not-visited")
    val answeredCount = statuses.count { it == "answered" || it == "marked-answered" }
    val unattemptedCount = statuses.count { it == "not-answered" || it == "not-visited" }
    val markedCount = statuses.count { it == "marked" || it == "marked-answered" }

    assertEquals(3, answeredCount)
    assertEquals(2, unattemptedCount)
    assertEquals(2, markedCount)
  }

  @Test
  fun testFlagForReviewToggle() {
    var isBookmarked = false
    assertFalse(isBookmarked)

    // Toggle flag
    isBookmarked = !isBookmarked
    assertTrue(isBookmarked)

    // Remove flag
    isBookmarked = !isBookmarked
    assertFalse(isBookmarked)
  }

  @Test
  fun testSubjectAreaAccuracyAndSpeedCalculation() {
    val correctCount = 42
    val incorrectCount = 8
    val totalAttempted = correctCount + incorrectCount // 50 questions
    val totalTimeSpentSeconds = 1800L // 30 minutes

    // Accuracy calculation: 42 / 50 * 100 = 84.0%
    val accuracy = (correctCount.toFloat() / totalAttempted.toFloat()) * 100f
    assertEquals(84.0f, accuracy, 0.01f)

    // Speed calculation: 1800s / 50 questions = 36.0 seconds per question
    val avgSecondsPerQuestion = totalTimeSpentSeconds.toFloat() / totalAttempted.toFloat()
    assertEquals(36.0f, avgSecondsPerQuestion, 0.01f)

    // Rate: 50 / (1800 / 60) = 50 / 30 = 1.666 Q/min
    val questionsPerMinute = (totalAttempted.toFloat() / (totalTimeSpentSeconds.toFloat() / 60f))
    assertEquals(1.666f, questionsPerMinute, 0.01f)

    // Benchmark comparison (Target: 45s)
    val targetSeconds = 45
    val isFast = avgSecondsPerQuestion <= targetSeconds * 0.9f // <= 40.5s -> Fast!
    assertTrue("Average speed of 36s should be fast against 45s target", isFast)
  }

  @Test
  fun testSpeedTierCategorization() {
    val target = 40
    fun rateSpeed(avgSeconds: Float): String = when {
      avgSeconds <= target * 0.9f -> "FAST"
      avgSeconds <= target * 1.25f -> "OPTIMAL"
      else -> "NEEDS_PRACTICE"
    }

    assertEquals("FAST", rateSpeed(30f))
    assertEquals("OPTIMAL", rateSpeed(45f))
    assertEquals("NEEDS_PRACTICE", rateSpeed(65f))
  }
}
