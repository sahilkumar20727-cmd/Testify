package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Summary data container for test submission confirmation.
 */
data class ExamSubmissionSummary(
  val testTitle: String,
  val totalQuestions: Int,
  val answeredCount: Int,
  val unattemptedCount: Int,
  val markedForReviewCount: Int,
  val notVisitedCount: Int,
  val remainingTimeFormatted: String
)

/**
 * Authentic exam submission confirmation modal dialog.
 * Shows detailed breakdown of attempted, unattempted, and marked questions
 * to prevent accidental test submissions.
 *
 * @param isVisible Controls dialog visibility.
 * @param summary Data model containing counts and test metadata.
 * @param onResumeTest Callback when user decides to continue the test.
 * @param onConfirmFinalSubmit Callback when user confirms final submission.
 */
@Composable
fun ExamSubmissionSummaryDialog(
  isVisible: Boolean,
  summary: ExamSubmissionSummary,
  onResumeTest: () -> Unit,
  onConfirmFinalSubmit: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (!isVisible) return

  Dialog(
    onDismissRequest = onResumeTest,
    properties = DialogProperties(
      dismissOnBackPress = true,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Surface(
      modifier = modifier
        .padding(horizontal = 20.dp, vertical = 24.dp)
        .fillMaxWidth()
        .testTag("exam_submission_summary_dialog"),
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF10131D),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2436)),
      shadowElevation = 8.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Icon header
        Box(
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1B4B))
            .border(1.dp, Color(0xFF4F46E5), CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AssignmentTurnedIn,
            contentDescription = null,
            tint = Color(0xFF818CF8),
            modifier = Modifier.size(28.dp)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "Test Submission Summary",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Black
        )

        Text(
          text = summary.testTitle,
          color = Color(0xFF94A3B8),
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Metric Cards (2x2 Grid)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            SummaryMetricCard(
              title = "Answered",
              count = summary.answeredCount,
              total = summary.totalQuestions,
              color = Color(0xFF10B981),
              icon = Icons.Default.CheckCircle,
              modifier = Modifier.weight(1f)
            )

            SummaryMetricCard(
              title = "Unattempted",
              count = summary.unattemptedCount,
              total = summary.totalQuestions,
              color = Color(0xFFF43F5E),
              icon = Icons.Outlined.Cancel,
              modifier = Modifier.weight(1f)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            SummaryMetricCard(
              title = "Marked Review",
              count = summary.markedForReviewCount,
              total = summary.totalQuestions,
              color = Color(0xFF8B5CF6),
              icon = Icons.Default.BookmarkBorder,
              modifier = Modifier.weight(1f)
            )

            SummaryMetricCard(
              title = "Not Visited",
              count = summary.notVisitedCount,
              total = summary.totalQuestions,
              color = Color(0xFF64748B),
              icon = Icons.Default.RadioButtonUnchecked,
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Time Remaining Pill
        Row(
          modifier = Modifier
            .background(Color(0xFF090B10), RoundedCornerShape(10.dp))
            .border(1.dp, Color(0xFF1E2436), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.HourglassBottom,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Time Remaining: ",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
          Text(
            text = summary.remainingTimeFormatted,
            color = Color(0xFFF59E0B),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Warning Alert Banner
        val hasPending = summary.unattemptedCount > 0 || summary.markedForReviewCount > 0
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (hasPending) Color(0xFF2D1515) else Color(0xFF0F231D))
            .border(
              1.dp,
              if (hasPending) Color(0xFF7F1D1D) else Color(0xFF065F46),
              RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
        ) {
          Row(verticalAlignment = Alignment.Top) {
            Icon(
              imageVector = if (hasPending) Icons.Outlined.WarningAmber else Icons.Default.CheckCircle,
              contentDescription = null,
              tint = if (hasPending) Color(0xFFF87171) else Color(0xFF34D399),
              modifier = Modifier
                .size(16.dp)
                .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (hasPending) {
                "You still have ${summary.unattemptedCount} unattempted questions and ${summary.markedForReviewCount} marked for review. Once submitted, answers cannot be edited."
              } else {
                "All ${summary.totalQuestions} questions have been answered. You can proceed with final submission."
              },
              color = if (hasPending) Color(0xFFFCA5A5) else Color(0xFFA7F3D0),
              fontSize = 11.sp,
              lineHeight = 16.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onResumeTest,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("resume_exam_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = Color(0xFFCBD5E1)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
          ) {
            Text(
              text = "Resume Test",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Button(
            onClick = onConfirmFinalSubmit,
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("confirm_final_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF10B981),
              contentColor = Color.White
            )
          ) {
            Text(
              text = "Yes, Submit",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SummaryMetricCard(
  title: String,
  count: Int,
  total: Int,
  color: Color,
  icon: ImageVector,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFF090B10))
      .border(1.dp, Color(0xFF1E2436), RoundedCornerShape(12.dp))
      .padding(10.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column {
        Text(
          text = title,
          color = Color(0xFF94A3B8),
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = count.toString(),
          color = color,
          fontSize = 18.sp,
          fontWeight = FontWeight.Black,
          fontFamily = FontFamily.Monospace
        )
      }

      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = color,
          modifier = Modifier.size(15.dp)
        )
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun ExamSubmissionSummaryDialogPreview() {
  ExamSubmissionSummaryDialog(
    isVisible = true,
    summary = ExamSubmissionSummary(
      testTitle = "SSC CGL Tier 1: Quant Foundation Mock 2026",
      totalQuestions = 25,
      answeredCount = 18,
      unattemptedCount = 5,
      markedForReviewCount = 2,
      notVisitedCount = 3,
      remainingTimeFormatted = "08:45"
    ),
    onResumeTest = {},
    onConfirmFinalSubmit = {}
  )
}
