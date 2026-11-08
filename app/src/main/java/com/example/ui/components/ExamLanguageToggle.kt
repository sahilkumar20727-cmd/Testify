package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

/**
 * Supported test languages for authentic bilingual examination viewing.
 */
enum class ExamLanguage(val code: String, val displayName: String, val nativeName: String) {
  ENGLISH(code = "en", displayName = "English", nativeName = "English"),
  HINDI(code = "hi", displayName = "Hindi", nativeName = "हिन्दी")
}

/**
 * Data model for bilingual questions supporting instant language switching.
 */
@Immutable
data class BilingualQuestionItem(
  val id: String,
  val questionEn: String,
  val questionHi: String,
  val optionsEn: List<String>,
  val optionsHi: List<String>,
  val explanationEn: String = "",
  val explanationHi: String = "",
  val correctAnswerIndex: Int = 0
) {
  fun getQuestionText(language: ExamLanguage): String {
    return if (language == ExamLanguage.HINDI && questionHi.isNotBlank()) questionHi else questionEn
  }

  fun getOptions(language: ExamLanguage): List<String> {
    return if (language == ExamLanguage.HINDI && optionsHi.isNotEmpty()) optionsHi else optionsEn
  }

  fun getExplanation(language: ExamLanguage): String {
    return if (language == ExamLanguage.HINDI && explanationHi.isNotBlank()) explanationHi else explanationEn
  }
}

/**
 * Premium bilingual toggle component placed at the top of the test screen,
 * allowing instant switching between Hindi and English question versions.
 *
 * @param selectedLanguage Currently active [ExamLanguage].
 * @param onLanguageChanged Callback invoked immediately upon user switching languages.
 * @param modifier Root modifier.
 * @param showIcon Whether to display the translation icon.
 * @param isCompact Compact format for tight app bars.
 */
@Composable
fun ExamLanguageToggle(
  selectedLanguage: ExamLanguage,
  onLanguageChanged: (ExamLanguage) -> Unit,
  modifier: Modifier = Modifier,
  showIcon: Boolean = true,
  isCompact: Boolean = false
) {
  val isHindi = selectedLanguage == ExamLanguage.HINDI

  Surface(
    modifier = modifier
      .testTag("exam_language_toggle")
      .semantics {
        contentDescription = "Language switcher: currently viewing in ${selectedLanguage.displayName}"
      },
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF090B10),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2436))
  ) {
    Row(
      modifier = Modifier
        .padding(3.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (showIcon) {
        Icon(
          imageVector = Icons.Default.Translate,
          contentDescription = null,
          tint = Color(0xFF818CF8),
          modifier = Modifier
            .padding(start = 6.dp, end = 4.dp)
            .size(if (isCompact) 14.dp else 16.dp)
        )
      }

      // English Pill
      LanguagePillItem(
        label = if (isCompact) "EN" else "English",
        isSelected = !isHindi,
        testTag = "toggle_lang_en",
        onClick = { onLanguageChanged(ExamLanguage.ENGLISH) }
      )

      Spacer(modifier = Modifier.width(3.dp))

      // Hindi Pill
      LanguagePillItem(
        label = if (isCompact) "HI" else "हिन्दी",
        isSelected = isHindi,
        testTag = "toggle_lang_hi",
        onClick = { onLanguageChanged(ExamLanguage.HINDI) }
      )
    }
  }
}

@Composable
private fun LanguagePillItem(
  label: String,
  isSelected: Boolean,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val backgroundColor by animateColorAsState(
    targetValue = if (isSelected) Color(0xFF4F46E5) else Color.Transparent,
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "pill_background"
  )

  val textColor by animateColorAsState(
    targetValue = if (isSelected) Color.White else Color(0xFF94A3B8),
    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
    label = "pill_text_color"
  )

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(backgroundColor)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = Color.White),
        onClick = onClick
      )
      .padding(horizontal = 10.dp, vertical = 5.dp)
      .testTag(testTag)
      .semantics {
        role = Role.RadioButton
        contentDescription = "$label language ${if (isSelected) "selected" else "not selected"}"
      },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
      fontFamily = FontFamily.SansSerif
    )
  }
}

/**
 * Top Exam Navigation Bar combining the [ExamLanguageToggle], Countdown Timer,
 * and Submit Button into a responsive exam header bar.
 */
@Composable
fun ExamTopHeaderBar(
  testTitle: String,
  selectedLanguage: ExamLanguage,
  remainingSeconds: Long,
  onLanguageChanged: (ExamLanguage) -> Unit,
  onSubmitClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .testTag("exam_top_header_bar"),
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF10131D),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2436)),
    shadowElevation = 4.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f, fill = false)) {
        Text(
          text = testTitle,
          color = Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1
        )
        Text(
          text = "TCS iON Authentic Interface",
          color = Color(0xFF818CF8),
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Language switcher toggle
        ExamLanguageToggle(
          selectedLanguage = selectedLanguage,
          onLanguageChanged = onLanguageChanged,
          isCompact = false
        )

        // Timer badge
        MockTestCountdownTimer(
          totalDurationSeconds = remainingSeconds,
          onTimeUp = onSubmitClick
        )
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun ExamLanguageTogglePreview() {
  var lang by remember { mutableStateOf(ExamLanguage.ENGLISH) }

  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    ExamLanguageToggle(
      selectedLanguage = lang,
      onLanguageChanged = { lang = it }
    )

    ExamTopHeaderBar(
      testTitle = "SSC CGL Tier 1: Percentage & Profit/Loss Mock",
      selectedLanguage = lang,
      remainingSeconds = 600,
      onLanguageChanged = { lang = it },
      onSubmitClick = {}
    )
  }
}
