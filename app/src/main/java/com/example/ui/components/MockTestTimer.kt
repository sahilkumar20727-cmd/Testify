package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Controller state for [MockTestCountdownTimer] and [VisualExamCountdownTimer]
 * allowing parent composables or viewmodels to observe and sync the exam timer
 * with local database storage.
 */
@Stable
class MockTestTimerState(
  val totalDurationSeconds: Long,
  initialRemainingSeconds: Long = totalDurationSeconds
) {
  var remainingSeconds by mutableLongStateOf(initialRemainingSeconds)
    internal set

  var isPaused by mutableStateOf(false)
  var isCompleted by mutableStateOf(false)
    internal set

  val progress: Float
    get() = if (totalDurationSeconds > 0) {
      (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

  val isWarning: Boolean
    get() = remainingSeconds in 61..300

  val isCritical: Boolean
    get() = remainingSeconds in 1..60

  fun pause() {
    isPaused = true
  }

  fun resume() {
    if (!isCompleted && remainingSeconds > 0) {
      isPaused = false
    }
  }

  fun reset(newDurationSeconds: Long = totalDurationSeconds) {
    remainingSeconds = newDurationSeconds
    isCompleted = false
    isPaused = false
  }

  fun updateRemainingSeconds(seconds: Long) {
    remainingSeconds = seconds.coerceAtLeast(0)
  }

  fun addTime(extraSeconds: Long) {
    if (!isCompleted) {
      remainingSeconds = (remainingSeconds + extraSeconds).coerceAtLeast(0)
    }
  }
}

@Composable
fun rememberMockTestTimerState(
  totalDurationSeconds: Long,
  initialRemainingSeconds: Long = totalDurationSeconds,
  key: Any? = totalDurationSeconds
): MockTestTimerState {
  return remember(key) {
    MockTestTimerState(
      totalDurationSeconds = totalDurationSeconds,
      initialRemainingSeconds = initialRemainingSeconds
    )
  }
}

/**
 * Reusable timer composable that displays a real-time countdown for active CBT / mock tests,
 * visual urgency color grading, and auto-submit callback when time expires.
 *
 * @param totalDurationSeconds Total duration of the mock test in seconds.
 * @param modifier Modifier for styling and layout.
 * @param initialRemainingSeconds Initial seconds count to support syncing from local database state.
 * @param isPaused When true, pauses the ticking of the timer.
 * @param warningThresholdSeconds Seconds below which visual alert changes to warning (amber).
 * @param criticalThresholdSeconds Seconds below which visual alert pulses critical (red).
 * @param showCircularProgress Whether to show a miniature circular indicator alongside digits.
 * @param onTick Optional callback invoked every second with remaining seconds for local state syncing.
 * @param onTimeUp Lambda triggered strictly when the countdown reaches 0 to auto-submit the exam.
 */
@Composable
fun MockTestCountdownTimer(
  totalDurationSeconds: Long,
  modifier: Modifier = Modifier,
  initialRemainingSeconds: Long = totalDurationSeconds,
  isPaused: Boolean = false,
  warningThresholdSeconds: Long = (totalDurationSeconds * 0.2f).toLong().coerceAtLeast(60L),
  criticalThresholdSeconds: Long = 30L,
  showCircularProgress: Boolean = true,
  testTag: String = "mock_test_countdown_timer",
  onTick: ((remainingSeconds: Long) -> Unit)? = null,
  onTimeUp: () -> Unit
) {
  var remainingSeconds by rememberSaveable(totalDurationSeconds, initialRemainingSeconds) {
    mutableLongStateOf(initialRemainingSeconds)
  }
  var hasSubmitted by rememberSaveable(totalDurationSeconds) {
    mutableStateOf(false)
  }

  val currentOnTimeUp by rememberUpdatedState(onTimeUp)
  val currentOnTick by rememberUpdatedState(onTick)

  // Countdown ticker effect
  LaunchedEffect(isPaused, remainingSeconds, hasSubmitted) {
    if (!isPaused && remainingSeconds > 0 && !hasSubmitted) {
      delay(1000L)
      val next = (remainingSeconds - 1).coerceAtLeast(0)
      remainingSeconds = next
      currentOnTick?.invoke(next)
      if (next <= 0 && !hasSubmitted) {
        hasSubmitted = true
        currentOnTimeUp()
      }
    } else if (remainingSeconds <= 0 && !hasSubmitted) {
      hasSubmitted = true
      currentOnTimeUp()
    }
  }

  val progress = if (totalDurationSeconds > 0) {
    (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
  } else 0f

  val isCritical = remainingSeconds in 1..criticalThresholdSeconds
  val isWarning = remainingSeconds in (criticalThresholdSeconds + 1)..warningThresholdSeconds

  // Dynamic color coding based on exam urgency
  val targetColor = when {
    isCritical -> Color(0xFFF43F5E) // Brand Rose/Red
    isWarning -> Color(0xFFF59E0B)  // Brand Amber
    else -> Color(0xFF10B981)      // Brand Emerald / Normal
  }

  val timerColor by animateColorAsState(
    targetValue = targetColor,
    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
    label = "timer_color_animation"
  )

  // Subtle pulsing animation when time is critically low (< 30s)
  val pulseAlpha = if (isCritical) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_transition")
    val alphaAnim by infiniteTransition.animateFloat(
      initialValue = 0.45f,
      targetValue = 1.0f,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
      ),
      label = "pulse_alpha"
    )
    alphaAnim
  } else 1.0f

  val formattedTime = formatCountdownTime(remainingSeconds)

  Surface(
    modifier = modifier
      .testTag(testTag)
      .semantics {
        contentDescription = "Exam timer: $formattedTime remaining"
      },
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF090B10),
    border = androidx.compose.foundation.BorderStroke(
      width = if (isCritical) 1.5.dp else 1.dp,
      color = timerColor.copy(alpha = if (isCritical) pulseAlpha else 0.45f)
    ),
    shadowElevation = if (isCritical) 4.dp else 1.dp
  ) {
    Row(
      modifier = Modifier
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      // Status Icon
      Icon(
        imageVector = when {
          isCritical -> Icons.Outlined.WarningAmber
          isWarning -> Icons.Default.HourglassBottom
          else -> Icons.Default.Timer
        },
        contentDescription = null,
        tint = timerColor,
        modifier = Modifier
          .size(16.dp)
          .alpha(pulseAlpha)
      )

      Spacer(modifier = Modifier.width(6.dp))

      // Digits Display
      Text(
        text = formattedTime,
        color = timerColor,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 0.5.sp,
        modifier = Modifier.alpha(if (isCritical) pulseAlpha else 1.0f)
      )

      // Circular remaining percentage ring
      if (showCircularProgress) {
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(14.dp)
        ) {
          CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(14.dp),
            color = timerColor,
            trackColor = timerColor.copy(alpha = 0.15f),
            strokeWidth = 2.dp,
            strokeCap = StrokeCap.Round
          )
        }
      }
    }
  }
}

/**
 * Full-width top banner version of the timer showing countdown, time remaining label,
 * and a smooth linear progress bar.
 */
@Composable
fun MockTestTimerBanner(
  totalDurationSeconds: Long,
  modifier: Modifier = Modifier,
  initialRemainingSeconds: Long = totalDurationSeconds,
  isPaused: Boolean = false,
  testTitle: String = "Mock Test",
  onTimeUp: () -> Unit
) {
  var remainingSeconds by rememberSaveable(totalDurationSeconds, initialRemainingSeconds) {
    mutableLongStateOf(initialRemainingSeconds)
  }
  var hasSubmitted by rememberSaveable(totalDurationSeconds) {
    mutableStateOf(false)
  }

  val currentOnTimeUp by rememberUpdatedState(onTimeUp)

  LaunchedEffect(isPaused, remainingSeconds, hasSubmitted) {
    if (!isPaused && remainingSeconds > 0 && !hasSubmitted) {
      delay(1000L)
      val next = (remainingSeconds - 1).coerceAtLeast(0)
      remainingSeconds = next
      if (next <= 0 && !hasSubmitted) {
        hasSubmitted = true
        currentOnTimeUp()
      }
    } else if (remainingSeconds <= 0 && !hasSubmitted) {
      hasSubmitted = true
      currentOnTimeUp()
    }
  }

  val progress = if (totalDurationSeconds > 0) {
    (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
  } else 0f

  val isCritical = remainingSeconds in 1..30L
  val isWarning = remainingSeconds in 31..60L

  val timerColor = when {
    isCritical -> Color(0xFFF43F5E)
    isWarning -> Color(0xFFF59E0B)
    else -> Color(0xFF10B981)
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFF0F121C))
      .padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = testTitle,
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.SemiBold
        )
        Text(
          text = if (isCritical) "Time running out! Auto-submitting soon" else "Time Remaining",
          color = if (isCritical) Color(0xFFF43F5E) else Color(0xFF94A3B8),
          fontSize = 10.sp,
          fontFamily = FontFamily.SansSerif
        )
      }

      MockTestCountdownTimer(
        totalDurationSeconds = totalDurationSeconds,
        initialRemainingSeconds = remainingSeconds,
        isPaused = isPaused,
        showCircularProgress = true,
        onTimeUp = currentOnTimeUp
      )
    }

    Spacer(modifier = Modifier.height(6.dp))

    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp)
        .clip(RoundedCornerShape(2.dp)),
      color = timerColor,
      trackColor = Color(0xFF1E2436),
      strokeCap = StrokeCap.Round
    )
  }
}

/**
 * Comprehensive visual countdown timer component that displays remaining exam duration,
 * triggers an automated warning banner when time is low, and continuously syncs
 * with local state (Room Database / ViewModel).
 *
 * @param totalDurationSeconds Total duration of the test in seconds.
 * @param initialRemainingSeconds Resumed remaining time from local persistence.
 * @param testTitle Title of current exam.
 * @param isPaused When true, pauses ticking.
 * @param warningThresholdSeconds Seconds below which warning banner triggers (default 300s = 5m).
 * @param criticalThresholdSeconds Seconds below which critical urgency triggers (default 60s = 1m).
 * @param flaggedCount Number of questions marked for review to show in the warning quick action.
 * @param onTimeUpdate Callback invoked on every second tick with remaining seconds to sync with local storage.
 * @param onLowTimeWarning Callback triggered when countdown crosses into the warning threshold.
 * @param onReviewFlaggedClicked Callback invoked when user taps "Review Flagged" in the warning banner.
 * @param onTimeUp Callback invoked when countdown reaches 0 to auto-submit the exam.
 */
@Composable
fun VisualExamCountdownTimer(
  totalDurationSeconds: Long,
  modifier: Modifier = Modifier,
  initialRemainingSeconds: Long = totalDurationSeconds,
  testTitle: String = "Mock Test",
  isPaused: Boolean = false,
  warningThresholdSeconds: Long = 300L,
  criticalThresholdSeconds: Long = 60L,
  flaggedCount: Int = 0,
  onTimeUpdate: ((remainingSeconds: Long) -> Unit)? = null,
  onLowTimeWarning: ((remainingSeconds: Long, isCritical: Boolean) -> Unit)? = null,
  onReviewFlaggedClicked: (() -> Unit)? = null,
  onTimeUp: () -> Unit
) {
  var remainingSeconds by rememberSaveable(totalDurationSeconds, initialRemainingSeconds) {
    mutableLongStateOf(initialRemainingSeconds)
  }
  var isWarningDismissed by rememberSaveable { mutableStateOf(false) }
  var hasTriggeredWarning by rememberSaveable { mutableStateOf(false) }
  var hasSubmitted by rememberSaveable { mutableStateOf(false) }

  val currentOnTimeUp by rememberUpdatedState(onTimeUp)
  val currentOnTimeUpdate by rememberUpdatedState(onTimeUpdate)
  val currentOnLowTimeWarning by rememberUpdatedState(onLowTimeWarning)

  val isCritical = remainingSeconds in 1..criticalThresholdSeconds
  val isWarning = remainingSeconds in (criticalThresholdSeconds + 1)..warningThresholdSeconds

  // Re-open warning if critical threshold is reached even if warning was dismissed earlier
  LaunchedEffect(isCritical) {
    if (isCritical) {
      isWarningDismissed = false
    }
  }

  // Ticker and Local State Synchronization
  LaunchedEffect(isPaused, remainingSeconds, hasSubmitted) {
    if (!isPaused && remainingSeconds > 0 && !hasSubmitted) {
      delay(1000L)
      val next = (remainingSeconds - 1).coerceAtLeast(0)
      remainingSeconds = next
      currentOnTimeUpdate?.invoke(next)

      if ((next <= warningThresholdSeconds) && !hasTriggeredWarning) {
        hasTriggeredWarning = true
        currentOnLowTimeWarning?.invoke(next, next <= criticalThresholdSeconds)
      }

      if (next <= 0 && !hasSubmitted) {
        hasSubmitted = true
        currentOnTimeUp()
      }
    } else if (remainingSeconds <= 0 && !hasSubmitted) {
      hasSubmitted = true
      currentOnTimeUp()
    }
  }

  val progress = if (totalDurationSeconds > 0) {
    (remainingSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)
  } else 0f

  val urgencyColor by animateColorAsState(
    targetValue = when {
      isCritical -> Color(0xFFF43F5E)
      isWarning -> Color(0xFFF59E0B)
      else -> Color(0xFF10B981)
    },
    label = "urgency_color"
  )

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("visual_countdown_timer")
      .semantics {
        contentDescription = "Visual countdown timer for $testTitle. Time remaining: ${formatCountdownTime(remainingSeconds)}"
      }
  ) {
    // Top Bar Timer Widget
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF0F121C),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2436)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Test info & Status Pill
        Column(modifier = Modifier.weight(1f, fill = false)) {
          Text(
            text = testTitle,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
          )
          Spacer(modifier = Modifier.height(2.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(urgencyColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = when {
                isCritical -> "CRITICAL TIME"
                isWarning -> "LOW TIME WARNING"
                else -> "IN PROGRESS"
              },
              color = urgencyColor,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Visual Countdown Display
        MockTestCountdownTimer(
          totalDurationSeconds = totalDurationSeconds,
          initialRemainingSeconds = remainingSeconds,
          isPaused = isPaused,
          warningThresholdSeconds = warningThresholdSeconds,
          criticalThresholdSeconds = criticalThresholdSeconds,
          showCircularProgress = true,
          onTimeUp = currentOnTimeUp
        )
      }
    }

    // Animated Low-Time Warning Alert Banner
    AnimatedVisibility(
      visible = (isWarning || isCritical) && !isWarningDismissed,
      enter = expandVertically(animationSpec = spring()) + fadeIn(),
      exit = shrinkVertically(animationSpec = spring()) + fadeOut()
    ) {
      LowTimeWarningBanner(
        remainingSeconds = remainingSeconds,
        isCritical = isCritical,
        flaggedCount = flaggedCount,
        onDismiss = { isWarningDismissed = true },
        onReviewFlagged = onReviewFlaggedClicked,
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}

/**
 * Distinct visual warning banner that appears when time drops below threshold,
 * prompting candidate action before auto-submit.
 */
@Composable
fun LowTimeWarningBanner(
  remainingSeconds: Long,
  isCritical: Boolean,
  flaggedCount: Int = 0,
  onDismiss: () -> Unit,
  onReviewFlagged: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "warning_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "warning_banner_pulse"
  )

  val backgroundColor = if (isCritical) Color(0xFF450A0A) else Color(0xFF451A03)
  val borderColor = if (isCritical) Color(0xFFF43F5E) else Color(0xFFF59E0B)
  val contentColor = if (isCritical) Color(0xFFFECDD3) else Color(0xFFFEF3C7)

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("low_time_warning_banner")
      .semantics {
        contentDescription = if (isCritical) {
          "Critical exam warning: less than one minute remaining. Prepare to submit."
        } else {
          "Low time warning: five minutes remaining. Review flagged questions."
        }
      },
    shape = RoundedCornerShape(14.dp),
    color = backgroundColor,
    border = androidx.compose.foundation.BorderStroke(
      width = if (isCritical) 1.5.dp else 1.dp,
      color = borderColor.copy(alpha = if (isCritical) pulseAlpha else 0.8f)
    ),
    shadowElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Outlined.WarningAmber,
          contentDescription = null,
          tint = borderColor,
          modifier = Modifier
            .size(22.dp)
            .alpha(if (isCritical) pulseAlpha else 1.0f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = if (isCritical) {
              "Final Countdown: ${formatCountdownTime(remainingSeconds)}"
            } else {
              "Time Running Low: ${formatCountdownTime(remainingSeconds)}"
            },
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isCritical) {
              "Auto-submitting very soon. Answers will be locked."
            } else {
              "Review flagged questions and finalize options now."
            },
            color = contentColor,
            fontSize = 11.sp,
            lineHeight = 14.sp
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        if (flaggedCount > 0 && onReviewFlagged != null) {
          OutlinedButton(
            onClick = onReviewFlagged,
            modifier = Modifier
              .testTag("warning_review_flagged_button")
              .semantics { role = Role.Button },
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Flag,
              contentDescription = null,
              tint = borderColor,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Review ($flaggedCount)",
              fontSize = 10.sp,
              color = borderColor,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
        }

        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss warning banner",
            tint = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }
  }
}

/**
 * Formats a duration in seconds into MM:SS or HH:MM:SS format.
 */
fun formatCountdownTime(totalSeconds: Long): String {
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

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun MockTestTimerPreview() {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Normal state
    MockTestCountdownTimer(
      totalDurationSeconds = 600,
      onTimeUp = {}
    )

    // Warning state (< 5 min)
    MockTestCountdownTimer(
      totalDurationSeconds = 600,
      initialRemainingSeconds = 250,
      onTimeUp = {}
    )

    // Critical state (< 30 sec)
    MockTestCountdownTimer(
      totalDurationSeconds = 600,
      initialRemainingSeconds = 25,
      onTimeUp = {}
    )

    // Full Visual Component with Low-Time Warning Banner
    VisualExamCountdownTimer(
      totalDurationSeconds = 600,
      initialRemainingSeconds = 180,
      flaggedCount = 3,
      onTimeUp = {}
    )
  }
}
