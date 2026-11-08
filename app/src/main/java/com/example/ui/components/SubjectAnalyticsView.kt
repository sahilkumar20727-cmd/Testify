package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Performance speed tiers benchmarked for standard national competitive examinations (SSC & Railway).
 */
enum class SpeedRating(
  val label: String,
  val labelHindi: String,
  val badgeColor: Color,
  val containerColor: Color
) {
  FAST(
    label = "Fast & Efficient",
    labelHindi = "तीव्र एवं दक्ष",
    badgeColor = Color(0xFF10B981),
    containerColor = Color(0xFF064E3B)
  ),
  OPTIMAL(
    label = "Optimal Pace",
    labelHindi = "संतुलित गति",
    badgeColor = Color(0xFF38BDF8),
    containerColor = Color(0xFF0C4A6E)
  ),
  MODERATE(
    label = "Moderate",
    labelHindi = "मध्यम",
    badgeColor = Color(0xFFF59E0B),
    containerColor = Color(0xFF451A03)
  ),
  NEEDS_PRACTICE(
    label = "Needs Practice",
    labelHindi = "अभ्यास आवश्यक",
    badgeColor = Color(0xFFF43F5E),
    containerColor = Color(0xFF4C0519)
  )
}

/**
 * Performance metrics for a single academic subject area.
 */
@Immutable
data class SubjectAnalyticsItem(
  val subjectId: String,
  val subjectName: String,
  val subjectCode: String,
  val examCategory: String, // e.g., "SSC CGL", "Railway NTPC"
  val testsAttempted: Int,
  val questionsAttempted: Int,
  val correctCount: Int,
  val incorrectCount: Int,
  val unattemptedCount: Int,
  val totalTimeSpentSeconds: Long,
  val targetSecondsPerQuestion: Int = 45
) {
  val accuracyPercent: Float
    get() = if (questionsAttempted > 0) {
      (correctCount.toFloat() / questionsAttempted.toFloat()) * 100f
    } else 0f

  val avgSecondsPerQuestion: Float
    get() = if (questionsAttempted > 0) {
      totalTimeSpentSeconds.toFloat() / questionsAttempted.toFloat()
    } else 0f

  val questionsPerMinute: Float
    get() = if (totalTimeSpentSeconds > 0) {
      (questionsAttempted.toFloat() / (totalTimeSpentSeconds.toFloat() / 60f))
    } else 0f

  val speedRating: SpeedRating
    get() = when {
      questionsAttempted == 0 -> SpeedRating.NEEDS_PRACTICE
      avgSecondsPerQuestion <= targetSecondsPerQuestion * 0.9f -> SpeedRating.FAST
      avgSecondsPerQuestion <= targetSecondsPerQuestion * 1.2f -> SpeedRating.OPTIMAL
      avgSecondsPerQuestion <= targetSecondsPerQuestion * 1.6f -> SpeedRating.MODERATE
      else -> SpeedRating.NEEDS_PRACTICE
    }

  val recommendation: String
    get() = when {
      accuracyPercent >= 80f && speedRating == SpeedRating.FAST ->
        "High accuracy with swift speed! Mastered section. Continue regular mock revisions."
      accuracyPercent >= 80f && (speedRating == SpeedRating.MODERATE || speedRating == SpeedRating.NEEDS_PRACTICE) ->
        "Excellent precision, but speed is holding you back. Practice shortcut techniques and timed sectional drills."
      accuracyPercent in 60f..79.9f && speedRating == SpeedRating.FAST ->
        "Answering rapidly, but negative marking is hurting your score. Reduce guesswork and verify options."
      accuracyPercent in 60f..79.9f ->
        "Balanced baseline. Strengthen core concepts and formula memorization to reach 85%+ accuracy."
      else ->
        "Priority focus area. Review foundational theory, analyze error patterns, and practice untimed topic quizzes."
    }
}

/**
 * High-level aggregated statistics across all subject areas.
 */
@Immutable
data class OverallAnalyticsSummary(
  val totalTestsAttempted: Int,
  val totalQuestionsSolved: Int,
  val totalCorrectAnswers: Int,
  val totalTimeSpentSeconds: Long,
  val subjectBreakdown: List<SubjectAnalyticsItem>
) {
  val overallAccuracy: Float
    get() = if (totalQuestionsSolved > 0) {
      (totalCorrectAnswers.toFloat() / totalQuestionsSolved.toFloat()) * 100f
    } else 0f

  val overallAvgSpeedSeconds: Float
    get() = if (totalQuestionsSolved > 0) {
      totalTimeSpentSeconds.toFloat() / totalQuestionsSolved.toFloat()
    } else 0f

  val strongestSubject: SubjectAnalyticsItem?
    get() = subjectBreakdown.maxByOrNull { it.accuracyPercent }

  val needsFocusSubject: SubjectAnalyticsItem?
    get() = subjectBreakdown.minByOrNull { it.accuracyPercent }
}

enum class AnalyticsSortOption(val title: String) {
  ALL("All Subjects"),
  HIGHEST_ACCURACY("Top Accuracy"),
  FASTEST_SPEED("Fastest Speed"),
  NEEDS_ATTENTION("Needs Attention")
}

/**
 * Reusable, comprehensive Subject Analytics Summary view for CBT examination platforms.
 *
 * Visualizes:
 * 1. Overall Key Performance Indicators (Accuracy %, Avg Pace sec/Q, Total Tests, Solved Count).
 * 2. Strongest vs. Needs-Attention subject callout cards.
 * 3. Sorting & filtering controls.
 * 4. Rich, card-based breakdown per subject area with accuracy progress bars,
 *    speed gauges, breakdown counters, and personalized actionable study advice.
 *
 * @param summary Aggregated analytics data model.
 * @param onNavigateBack Optional back callback.
 * @param onSubjectClick Callback when user taps a specific subject card to drill down.
 * @param modifier Custom layout modifier.
 * @param isBilingual When true, presents bilingual labels (Hindi & English).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubjectAnalyticsSummaryView(
  summary: OverallAnalyticsSummary,
  modifier: Modifier = Modifier,
  onNavigateBack: (() -> Unit)? = null,
  onSubjectClick: ((subjectId: String) -> Unit)? = null,
  isBilingual: Boolean = false
) {
  var selectedSort by remember { mutableStateOf(AnalyticsSortOption.ALL) }

  val sortedSubjects by remember(summary.subjectBreakdown, selectedSort) {
    derivedStateOf {
      when (selectedSort) {
        AnalyticsSortOption.ALL -> summary.subjectBreakdown
        AnalyticsSortOption.HIGHEST_ACCURACY -> summary.subjectBreakdown.sortedByDescending { it.accuracyPercent }
        AnalyticsSortOption.FASTEST_SPEED -> summary.subjectBreakdown.sortedBy { it.avgSecondsPerQuestion }
        AnalyticsSortOption.NEEDS_ATTENTION -> summary.subjectBreakdown.sortedBy { it.accuracyPercent }
      }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF07090E))
      .padding(horizontal = 16.dp)
      .testTag("subject_analytics_view")
      .semantics { contentDescription = "Subject area accuracy and speed performance analytics" },
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
  ) {
    // Top Bar Header
    item(key = "header") {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (onNavigateBack != null) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier
                .size(40.dp)
                .testTag("analytics_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Navigate back",
                tint = Color.White
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
          }
          Column {
            Text(
              text = if (isBilingual) "Performance Analytics / विश्लेषण" else "Performance Analytics",
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              fontFamily = FontFamily.SansSerif
            )
            Text(
              text = "Accuracy & Speed per Subject Area",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFF1E1B4B),
          border = BorderStroke(1.dp, Color(0xFF4338CA))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.TrendingUp,
              contentDescription = null,
              tint = Color(0xFFA5B4FC),
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${summary.totalTestsAttempted} Tests Taken",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFE0E7FF),
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // Hero KPI Banner
    item(key = "hero_kpis") {
      OverallKpiCard(
        summary = summary,
        isBilingual = isBilingual
      )
    }

    // Strengths vs Focus Callouts
    item(key = "insights_callouts") {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        summary.strongestSubject?.let { strong ->
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF064E3B).copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.5f))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF34D399),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Strongest Area",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF34D399)
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = strong.subjectName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1
              )
              Text(
                text = "${String.format(Locale.US, "%.1f", strong.accuracyPercent)}% Accuracy • ${String.format(Locale.US, "%.0f", strong.avgSecondsPerQuestion)}s/Q",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA7F3D0),
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        summary.needsFocusSubject?.let { weak ->
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF451A03).copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f))
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = null,
                  tint = Color(0xFFFBBF24),
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Focus Target",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFFFBBF24)
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = weak.subjectName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1
              )
              Text(
                text = "${String.format(Locale.US, "%.1f", weak.accuracyPercent)}% Accuracy • ${String.format(Locale.US, "%.0f", weak.avgSecondsPerQuestion)}s/Q",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFDE68A),
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }
    }

    // Filter Chips Row
    item(key = "filter_chips") {
      Column {
        Text(
          text = "Subject Area Breakdown",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFCBD5E1),
          textDecoration = null
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          AnalyticsSortOption.values().forEach { option ->
            val isSelected = selectedSort == option
            FilterChip(
              selected = isSelected,
              onClick = { selectedSort = option },
              label = {
                Text(
                  text = option.title,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF312E81),
                selectedLabelColor = Color(0xFFC7D2FE),
                containerColor = Color(0xFF141824),
                labelColor = Color(0xFF94A3B8)
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = isSelected,
                borderColor = Color(0xFF1E2436),
                selectedBorderColor = Color(0xFF6366F1)
              )
            )
          }
        }
      }
    }

    // List of Subject Cards
    items(sortedSubjects, key = { it.subjectId }) { subject ->
      SubjectAnalyticsCard(
        subject = subject,
        onClick = onSubjectClick?.let { { it(subject.subjectId) } }
      )
    }
  }
}

/**
 * Top Card displaying Overall Accuracy, Average Speed, and Total Questions Solved.
 */
@Composable
private fun OverallKpiCard(
  summary: OverallAnalyticsSummary,
  isBilingual: Boolean
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = Color(0xFF0F121C),
    border = BorderStroke(1.dp, Color(0xFF1E2436)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "OVERALL EXAM READINESS",
          fontSize = 11.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFF818CF8),
          letterSpacing = 1.sp
        )
        Text(
          text = "${summary.totalQuestionsSolved} Questions Evaluated",
          fontSize = 11.sp,
          color = Color(0xFF64748B),
          fontFamily = FontFamily.Monospace
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Overall Accuracy
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
              progress = { (summary.overallAccuracy / 100f).coerceIn(0f, 1f) },
              modifier = Modifier.size(68.dp),
              color = Color(0xFF10B981),
              trackColor = Color(0xFF132A24),
              strokeWidth = 7.dp,
              strokeCap = StrokeCap.Round
            )
            Text(
              text = "${String.format(Locale.US, "%.0f", summary.overallAccuracy)}%",
              fontSize = 15.sp,
              fontWeight = FontWeight.Black,
              color = Color.White
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Overall Accuracy",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCBD5E1)
          )
        }

        // Overall Average Speed
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
              progress = {
                // benchmark: 60s max for full ring
                (1f - (summary.overallAvgSpeedSeconds / 90f)).coerceIn(0.1f, 1f)
              },
              modifier = Modifier.size(68.dp),
              color = Color(0xFF38BDF8),
              trackColor = Color(0xFF11293A),
              strokeWidth = 7.dp,
              strokeCap = StrokeCap.Round
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "${String.format(Locale.US, "%.0f", summary.overallAvgSpeedSeconds)}s",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = "per Q",
                fontSize = 9.sp,
                color = Color(0xFF94A3B8)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Average Pace",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCBD5E1)
          )
        }

        // Total Tests Solved
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color(0xFF1E1B4B))
              .border(1.dp, Color(0xFF4338CA), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = summary.totalCorrectAnswers.toString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFA5B4FC)
              )
              Text(
                text = "Correct",
                fontSize = 9.sp,
                color = Color(0xFF818CF8)
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Total Correct",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCBD5E1)
          )
        }
      }
    }
  }
}

/**
 * Detailed performance breakdown card for a single academic subject area.
 */
@Composable
fun SubjectAnalyticsCard(
  subject: SubjectAnalyticsItem,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  val accuracyColor by animateColorAsState(
    targetValue = when {
      subject.accuracyPercent >= 80f -> Color(0xFF10B981)
      subject.accuracyPercent >= 60f -> Color(0xFFF59E0B)
      else -> Color(0xFFF43F5E)
    },
    label = "subject_accuracy_color"
  )

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("analytics_subject_card_${subject.subjectId}")
      .semantics {
        role = Role.Button
        contentDescription = "${subject.subjectName} analytics: ${String.format(Locale.US, "%.1f", subject.accuracyPercent)} percent accuracy, ${String.format(Locale.US, "%.0f", subject.avgSecondsPerQuestion)} seconds per question"
      }
      .then(
        if (onClick != null) {
          Modifier.clickable(onClick = onClick)
        } else Modifier
      ),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF10131E),
    border = BorderStroke(1.dp, Color(0xFF1E2436))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      
      // Header: Subject Name, Code & Speed Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = subject.subjectName,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFF1A1F30)
            ) {
              Text(
                text = subject.subjectCode,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = "${subject.examCategory} • ${subject.testsAttempted} Tests Attempted",
            fontSize = 11.sp,
            color = Color(0xFF64748B)
          )
        }

        // Speed Rating Pill
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = subject.speedRating.containerColor,
          border = BorderStroke(1.dp, subject.speedRating.badgeColor.copy(alpha = 0.6f))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = null,
              tint = subject.speedRating.badgeColor,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = subject.speedRating.label,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = subject.speedRating.badgeColor
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Accuracy Progress Gauge Bar
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Accuracy",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFCBD5E1)
          )
          Text(
            text = "${String.format(Locale.US, "%.1f", subject.accuracyPercent)}%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = accuracyColor,
            fontFamily = FontFamily.Monospace
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { (subject.accuracyPercent / 100f).coerceIn(0f, 1f) },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = accuracyColor,
          trackColor = Color(0xFF181D2B),
          strokeCap = StrokeCap.Round
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Speed & Counts Grid
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF090B12),
        border = BorderStroke(1.dp, Color(0xFF181C28)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Speed per Q
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${String.format(Locale.US, "%.0f", subject.avgSecondsPerQuestion)}s / Q",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = Color.White,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Target: ${subject.targetSecondsPerQuestion}s",
              fontSize = 9.sp,
              color = Color(0xFF64748B)
            )
          }

          Box(
            modifier = Modifier
              .height(24.dp)
              .width(1.dp)
              .background(Color(0xFF1E2436))
          )

          // Rate (Q/min)
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "${String.format(Locale.US, "%.1f", subject.questionsPerMinute)} Q/m",
              fontSize = 12.sp,
              fontWeight = FontWeight.Black,
              color = Color(0xFF38BDF8),
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = "Answering Rate",
              fontSize = 9.sp,
              color = Color(0xFF64748B)
            )
          }

          Box(
            modifier = Modifier
              .height(24.dp)
              .width(1.dp)
              .background(Color(0xFF1E2436))
          )

          // Correct / Wrong
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "${subject.correctCount}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF34D399),
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = " / ",
                fontSize = 10.sp,
                color = Color(0xFF64748B)
              )
              Text(
                text = "${subject.incorrectCount}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFF43F5E),
                fontFamily = FontFamily.Monospace
              )
            }
            Text(
              text = "Right / Wrong",
              fontSize = 9.sp,
              color = Color(0xFF64748B)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Recommendation text box
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF141824),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
          verticalAlignment = Alignment.Top
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = Color(0xFF818CF8),
            modifier = Modifier
              .size(14.dp)
              .padding(top = 1.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = subject.recommendation,
            fontSize = 11.sp,
            color = Color(0xFFCBD5E1),
            lineHeight = 15.sp
          )
        }
      }
    }
  }
}

@Preview(name = "Analytics Screen", showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun SubjectAnalyticsPreview() {
  val sampleSummary = OverallAnalyticsSummary(
    totalTestsAttempted = 14,
    totalQuestionsSolved = 180,
    totalCorrectAnswers = 148,
    totalTimeSpentSeconds = 6240L,
    subjectBreakdown = listOf(
      SubjectAnalyticsItem(
        subjectId = "sub_quant",
        subjectName = "Quantitative Aptitude",
        subjectCode = "MATH",
        examCategory = "SSC CGL",
        testsAttempted = 5,
        questionsAttempted = 60,
        correctCount = 51,
        incorrectCount = 9,
        unattemptedCount = 0,
        totalTimeSpentSeconds = 2460L,
        targetSecondsPerQuestion = 50
      ),
      SubjectAnalyticsItem(
        subjectId = "sub_reasoning",
        subjectName = "General Intelligence & Reasoning",
        subjectCode = "REAS",
        examCategory = "SSC CGL & Railway",
        testsAttempted = 4,
        questionsAttempted = 50,
        correctCount = 46,
        incorrectCount = 4,
        unattemptedCount = 0,
        totalTimeSpentSeconds = 1600L,
        targetSecondsPerQuestion = 40
      ),
      SubjectAnalyticsItem(
        subjectId = "sub_english",
        subjectName = "English Comprehension",
        subjectCode = "ENG",
        examCategory = "SSC CGL",
        testsAttempted = 3,
        questionsAttempted = 40,
        correctCount = 31,
        incorrectCount = 9,
        unattemptedCount = 0,
        totalTimeSpentSeconds = 1120L,
        targetSecondsPerQuestion = 30
      ),
      SubjectAnalyticsItem(
        subjectId = "sub_ga",
        subjectName = "General Awareness & Science",
        subjectCode = "GA",
        examCategory = "Railway & SSC",
        testsAttempted = 2,
        questionsAttempted = 30,
        correctCount = 20,
        incorrectCount = 10,
        unattemptedCount = 0,
        totalTimeSpentSeconds = 1060L,
        targetSecondsPerQuestion = 20
      )
    )
  )

  SubjectAnalyticsSummaryView(summary = sampleSummary)
}
