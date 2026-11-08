package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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

/**
 * Authentic examination status types following standard TCS iON patterns.
 */
enum class QuestionStatus(
  val label: String,
  val labelHindi: String,
  val containerColor: Color,
  val contentColor: Color,
  val borderColor: Color
) {
  ANSWERED(
    label = "Answered",
    labelHindi = "उत्तर दिया",
    containerColor = Color(0xFF10B981),
    contentColor = Color.White,
    borderColor = Color(0xFF34D399)
  ),
  NOT_ANSWERED(
    label = "Not Answered",
    labelHindi = "उत्तर नहीं दिया",
    containerColor = Color(0xFFF43F5E),
    contentColor = Color.White,
    borderColor = Color(0xFFFB7185)
  ),
  MARKED_FOR_REVIEW(
    label = "Marked for Review",
    labelHindi = "समीक्षा के लिए चिह्नित",
    containerColor = Color(0xFF8B5CF6),
    contentColor = Color.White,
    borderColor = Color(0xFFA78BFA)
  ),
  ANSWERED_AND_MARKED(
    label = "Answered & Marked",
    labelHindi = "उत्तर दिया एवं चिह्नित",
    containerColor = Color(0xFF7C3AED),
    contentColor = Color.White,
    borderColor = Color(0xFFC4B5FD)
  ),
  NOT_VISITED(
    label = "Not Visited",
    labelHindi = "नहीं देखा",
    containerColor = Color(0xFF171B28),
    contentColor = Color(0xFF94A3B8),
    borderColor = Color(0xFF283045)
  )
}

/**
 * State of an individual question item in the palette.
 */
@Immutable
data class QuestionPaletteItem(
  val index: Int,
  val number: Int = index + 1,
  val status: QuestionStatus = QuestionStatus.NOT_VISITED,
  val isBookmarked: Boolean = false,
  val isCurrent: Boolean = false
)

enum class PaletteFilter(val title: String) {
  ALL("All"),
  ANSWERED("Answered"),
  NOT_ANSWERED("Unanswered"),
  MARKED("Marked")
}

/**
 * Reusable Question Navigation Palette component for mock tests and CBT exams.
 *
 * Displays:
 * 1. Palette header with question counts and section title.
 * 2. Status legend with live counts for Answered, Unanswered, Marked, and Not Visited.
 * 3. Quick filter tabs (All, Answered, Unanswered, Marked).
 * 4. Responsive grid of clickable question cells with status indicators,
 *    current question selection ring, and bookmark badges.
 *
 * @param questions List of [QuestionPaletteItem] representing all questions in the test.
 * @param currentQuestionIndex 0-based index of the currently active question.
 * @param onQuestionClick Lambda invoked with the 0-based index when a user selects a question.
 * @param modifier Modifier for root container.
 * @param title Section or test title (e.g., "Quantitative Aptitude").
 * @param isBilingual When true, displays Hindi alongside English in the legend.
 * @param showFilterChips Whether to show filtering chips at the top.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionNavigationPalette(
  questions: List<QuestionPaletteItem>,
  currentQuestionIndex: Int,
  onQuestionClick: (index: Int) -> Unit,
  modifier: Modifier = Modifier,
  title: String = "Question Palette",
  isBilingual: Boolean = false,
  showFilterChips: Boolean = true
) {
  var selectedFilter by remember { mutableStateOf(PaletteFilter.ALL) }

  // Compute live statistics for legend
  val answeredCount by remember(questions) {
    derivedStateOf {
      questions.count {
        it.status == QuestionStatus.ANSWERED || it.status == QuestionStatus.ANSWERED_AND_MARKED
      }
    }
  }

  val notAnsweredCount by remember(questions) {
    derivedStateOf {
      questions.count { it.status == QuestionStatus.NOT_ANSWERED }
    }
  }

  val markedCount by remember(questions) {
    derivedStateOf {
      questions.count {
        it.status == QuestionStatus.MARKED_FOR_REVIEW ||
            it.status == QuestionStatus.ANSWERED_AND_MARKED ||
            it.isBookmarked
      }
    }
  }

  val notVisitedCount by remember(questions) {
    derivedStateOf {
      questions.count { it.status == QuestionStatus.NOT_VISITED }
    }
  }

  // Filtered list
  val filteredQuestions by remember(questions, selectedFilter) {
    derivedStateOf {
      when (selectedFilter) {
        PaletteFilter.ALL -> questions
        PaletteFilter.ANSWERED -> questions.filter {
          it.status == QuestionStatus.ANSWERED || it.status == QuestionStatus.ANSWERED_AND_MARKED
        }
        PaletteFilter.NOT_ANSWERED -> questions.filter {
          it.status == QuestionStatus.NOT_ANSWERED || it.status == QuestionStatus.NOT_VISITED
        }
        PaletteFilter.MARKED -> questions.filter {
          it.status == QuestionStatus.MARKED_FOR_REVIEW ||
              it.status == QuestionStatus.ANSWERED_AND_MARKED ||
              it.isBookmarked
        }
      }
    }
  }

  Surface(
    modifier = modifier
      .testTag("question_navigation_palette")
      .semantics { contentDescription = "Exam question navigation palette" },
    shape = RoundedCornerShape(16.dp),
    color = Color(0xFF10131D),
    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2436)),
    shadowElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp)
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Outlined.TableChart,
            contentDescription = null,
            tint = Color(0xFF818CF8),
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Text(
          text = "${questions.size} Questions",
          color = Color(0xFF818CF8),
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier
            .background(Color(0xFF1E1B4B), RoundedCornerShape(6.dp))
            .border(1.dp, Color(0xFF3730A3), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Status Legend
      QuestionPaletteLegend(
        answeredCount = answeredCount,
        notAnsweredCount = notAnsweredCount,
        markedCount = markedCount,
        notVisitedCount = notVisitedCount,
        isBilingual = isBilingual
      )

      // Filter chips
      if (showFilterChips) {
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          PaletteFilter.values().forEach { filter ->
            val isSelected = filter == selectedFilter
            FilterChip(
              selected = isSelected,
              onClick = { selectedFilter = filter },
              label = {
                Text(
                  text = filter.title,
                  fontSize = 11.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF312E81),
                selectedLabelColor = Color(0xFFC7D2FE),
                containerColor = Color(0xFF171B28),
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

      Spacer(modifier = Modifier.height(12.dp))

      // Grid of Question Cells
      if (filteredQuestions.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No questions match the selected filter.",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
          )
        }
      } else {
        LazyVerticalGrid(
          columns = GridCells.Adaptive(minSize = 44.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          contentPadding = PaddingValues(bottom = 4.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(filteredQuestions, key = { it.index }) { item ->
            QuestionPaletteCell(
              item = item,
              isCurrent = item.index == currentQuestionIndex,
              onClick = { onQuestionClick(item.index) }
            )
          }
        }
      }
    }
  }
}

/**
 * Individual clickable question number cell in the palette.
 */
@Composable
fun QuestionPaletteCell(
  item: QuestionPaletteItem,
  isCurrent: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val status = item.status
  val currentRingColor by animateColorAsState(
    targetValue = if (isCurrent) Color(0xFF38BDF8) else Color.Transparent,
    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    label = "current_cell_ring"
  )

  Box(
    modifier = modifier
      .size(46.dp)
      .aspectRatio(1f)
      .testTag("question_palette_cell_${item.number}")
      .semantics {
        role = Role.Button
        contentDescription = "Question ${item.number}, Status: ${status.label}${if (isCurrent) ", Current question" else ""}"
      }
      .clip(RoundedCornerShape(10.dp))
      .background(status.containerColor)
      .border(
        width = if (isCurrent) 2.5.dp else 1.dp,
        color = if (isCurrent) currentRingColor else status.borderColor,
        shape = RoundedCornerShape(10.dp)
      )
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = Color.White),
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    // Question number
    Text(
      text = item.number.toString(),
      color = status.contentColor,
      fontSize = 13.sp,
      fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
      fontFamily = FontFamily.SansSerif
    )

    // Answered & Marked dot indicator (authentic TCS iON green pip at top-right)
    if (status == QuestionStatus.ANSWERED_AND_MARKED) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(3.dp)
          .size(7.dp)
          .clip(CircleShape)
          .background(Color(0xFF22C55E))
          .border(1.dp, Color.White, CircleShape)
      )
    }

    // Bookmark star badge at top-left
    if (item.isBookmarked) {
      Icon(
        imageVector = Icons.Default.Star,
        contentDescription = "Bookmarked",
        tint = Color(0xFFFBBF24),
        modifier = Modifier
          .align(Alignment.TopStart)
          .padding(2.dp)
          .size(9.dp)
      )
    }
  }
}

/**
 * Visual status legend showing status counts and color swatches.
 */
@Composable
fun QuestionPaletteLegend(
  answeredCount: Int,
  notAnsweredCount: Int,
  markedCount: Int,
  notVisitedCount: Int,
  isBilingual: Boolean = false,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(Color(0xFF090B10), RoundedCornerShape(10.dp))
      .border(1.dp, Color(0xFF1E2436), RoundedCornerShape(10.dp))
      .padding(10.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      LegendItem(
        color = Color(0xFF10B981),
        count = answeredCount,
        label = if (isBilingual) "Answered (उत्तर दिया)" else "Answered",
        modifier = Modifier.weight(1f)
      )
      LegendItem(
        color = Color(0xFFF43F5E),
        count = notAnsweredCount,
        label = if (isBilingual) "Not Answered (अनुत्तरित)" else "Not Answered",
        modifier = Modifier.weight(1f)
      )
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      LegendItem(
        color = Color(0xFF8B5CF6),
        count = markedCount,
        label = if (isBilingual) "Marked (समीक्षा)" else "Marked Review",
        modifier = Modifier.weight(1f)
      )
      LegendItem(
        color = Color(0xFF334155),
        count = notVisitedCount,
        label = if (isBilingual) "Not Visited (नहीं देखा)" else "Not Visited",
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun LegendItem(
  color: Color,
  count: Int,
  label: String,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.padding(horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(color)
        .border(1.dp, color.copy(alpha = 0.5f), CircleShape)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = "$count $label",
      color = Color(0xFF94A3B8),
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium,
      maxLines = 1
    )
  }
}

/**
 * Bottom Sheet container for mobile displays allowing the student to inspect the palette
 * without obscuring the question and options on compact screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionPaletteBottomSheet(
  isVisible: Boolean,
  questions: List<QuestionPaletteItem>,
  currentQuestionIndex: Int,
  onQuestionClick: (index: Int) -> Unit,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
  if (isVisible) {
    ModalBottomSheet(
      onDismissRequest = onDismissRequest,
      sheetState = sheetState,
      containerColor = Color(0xFF10131D),
      contentColor = Color.White,
      dragHandle = {
        Box(
          modifier = Modifier
            .padding(top = 10.dp, bottom = 6.dp)
            .width(36.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF334155))
        )
      },
      modifier = modifier
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Question Palette",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          IconButton(
            onClick = onDismissRequest,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close palette",
              tint = Color(0xFF94A3B8)
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        QuestionNavigationPalette(
          questions = questions,
          currentQuestionIndex = currentQuestionIndex,
          onQuestionClick = { idx ->
            onQuestionClick(idx)
            onDismissRequest()
          },
          showFilterChips = true
        )

        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun QuestionPalettePreview() {
  val sampleQuestions = listOf(
    QuestionPaletteItem(0, status = QuestionStatus.ANSWERED),
    QuestionPaletteItem(1, status = QuestionStatus.ANSWERED),
    QuestionPaletteItem(2, status = QuestionStatus.NOT_ANSWERED),
    QuestionPaletteItem(3, status = QuestionStatus.MARKED_FOR_REVIEW, isBookmarked = true),
    QuestionPaletteItem(4, status = QuestionStatus.ANSWERED_AND_MARKED, isCurrent = true),
    QuestionPaletteItem(5, status = QuestionStatus.NOT_VISITED),
    QuestionPaletteItem(6, status = QuestionStatus.NOT_VISITED),
    QuestionPaletteItem(7, status = QuestionStatus.NOT_VISITED),
    QuestionPaletteItem(8, status = QuestionStatus.NOT_VISITED),
    QuestionPaletteItem(9, status = QuestionStatus.NOT_VISITED)
  )

  Column(modifier = Modifier.padding(16.dp)) {
    QuestionNavigationPalette(
      questions = sampleQuestions,
      currentQuestionIndex = 4,
      onQuestionClick = {}
    )
  }
}
