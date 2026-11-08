package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable 'Flag for Review' button for examination and test-taking interfaces.
 *
 * Allows candidates to flag challenging or doubtful questions so they can easily
 * jump back to them via the Question Navigation Palette before final test submission.
 *
 * @param isFlagged Whether the active question is currently flagged/marked for review.
 * @param onToggleFlag Callback invoked when the candidate clicks to toggle the flag status.
 * @param modifier Custom modifier.
 * @param isBilingual When true, displays bilingual labels (English and Hindi).
 * @param compact When true, renders a slightly more compact format suitable for toolbars.
 */
@Composable
fun FlagForReviewButton(
  isFlagged: Boolean,
  onToggleFlag: () -> Unit,
  modifier: Modifier = Modifier,
  isBilingual: Boolean = false,
  compact: Boolean = false
) {
  val containerColor by animateColorAsState(
    targetValue = if (isFlagged) Color(0xFF451A03) else Color(0xFF141824),
    label = "flag_btn_bg"
  )
  val contentColor by animateColorAsState(
    targetValue = if (isFlagged) Color(0xFFFBBF24) else Color(0xFFCBD5E1),
    label = "flag_btn_content"
  )
  val borderColor by animateColorAsState(
    targetValue = if (isFlagged) Color(0xFFF59E0B) else Color(0xFF334155),
    label = "flag_btn_border"
  )

  OutlinedButton(
    onClick = onToggleFlag,
    modifier = modifier
      .defaultMinSize(minHeight = 48.dp)
      .testTag("flag_for_review_button")
      .semantics {
        role = Role.Button
        contentDescription = if (isFlagged) {
          "Question is flagged for review. Tap to remove flag."
        } else {
          "Flag question for review"
        }
      },
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = containerColor,
      contentColor = contentColor
    ),
    border = BorderStroke(1.2.dp, borderColor),
    contentPadding = if (compact) {
      PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    } else {
      PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    }
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = if (isFlagged) Icons.Filled.Flag else Icons.Outlined.Flag,
        contentDescription = null,
        tint = if (isFlagged) Color(0xFFFBBF24) else contentColor,
        modifier = Modifier.size(if (compact) 16.dp else 18.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = when {
          isBilingual && isFlagged -> "Flagged / चिह्नित"
          isBilingual -> "Flag for Review / समीक्षा"
          isFlagged -> "Flagged for Review"
          else -> "Flag for Review"
        },
        fontSize = if (compact) 12.sp else 13.sp,
        fontWeight = if (isFlagged) FontWeight.Bold else FontWeight.SemiBold,
        color = contentColor
      )
    }
  }
}

@Preview(name = "Unflagged", showBackground = true, backgroundColor = 0xFF0D0F17)
@Composable
private fun FlagForReviewButtonUnflaggedPreview() {
  FlagForReviewButton(
    isFlagged = false,
    onToggleFlag = {}
  )
}

@Preview(name = "Flagged", showBackground = true, backgroundColor = 0xFF0D0F17)
@Composable
private fun FlagForReviewButtonFlaggedPreview() {
  FlagForReviewButton(
    isFlagged = true,
    onToggleFlag = {}
  )
}
