package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository layer abstracting Room Database data access for test progress sessions.
 */
class TestProgressRepository(private val dao: TestProgressDao) {

  fun observeTestProgress(testId: String): Flow<TestProgressEntity?> {
    return dao.observeTestProgress(testId)
  }

  fun observeAllActiveSessions(): Flow<List<TestProgressEntity>> {
    return dao.observeAllActiveSessions()
  }

  suspend fun getTestProgress(testId: String): TestProgressEntity? {
    return withContext(Dispatchers.IO) {
      dao.getTestProgress(testId)
    }
  }

  suspend fun saveProgress(
    testId: String,
    testTitle: String,
    studentId: String = "usr_sahil",
    totalDurationSeconds: Long,
    remainingSeconds: Long,
    activeQuestionIndex: Int,
    userAnswersJson: String,
    isCompleted: Boolean = false
  ) {
    withContext(Dispatchers.IO) {
      val entity = TestProgressEntity(
        testId = testId,
        studentId = studentId,
        testTitle = testTitle,
        totalDurationSeconds = totalDurationSeconds,
        remainingSeconds = remainingSeconds,
        activeQuestionIndex = activeQuestionIndex,
        userAnswersJson = userAnswersJson,
        lastUpdatedTimestamp = System.currentTimeMillis(),
        isCompleted = isCompleted
      )
      dao.saveTestProgress(entity)
    }
  }

  suspend fun deleteProgress(testId: String) {
    withContext(Dispatchers.IO) {
      dao.deleteTestProgress(testId)
    }
  }

  suspend fun clearAll() {
    withContext(Dispatchers.IO) {
      dao.clearAllTestProgress()
    }
  }
}
