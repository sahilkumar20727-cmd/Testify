package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing the in-progress state of an active mock test,
 * allowing students to safely resume their test after closing or backgrounding the app.
 */
@Entity(tableName = "test_progress")
data class TestProgressEntity(
  @PrimaryKey
  val testId: String,
  val studentId: String = "usr_sahil",
  val testTitle: String,
  val totalDurationSeconds: Long,
  val remainingSeconds: Long,
  val activeQuestionIndex: Int = 0,
  val userAnswersJson: String = "{}",
  val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
  val isCompleted: Boolean = false
)
