package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Room operations on [TestProgressEntity].
 */
@Dao
interface TestProgressDao {

  @Query("SELECT * FROM test_progress WHERE testId = :testId LIMIT 1")
  fun observeTestProgress(testId: String): Flow<TestProgressEntity?>

  @Query("SELECT * FROM test_progress WHERE testId = :testId LIMIT 1")
  suspend fun getTestProgress(testId: String): TestProgressEntity?

  @Query("SELECT * FROM test_progress WHERE isCompleted = 0 ORDER BY lastUpdatedTimestamp DESC")
  fun observeAllActiveSessions(): Flow<List<TestProgressEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveTestProgress(progress: TestProgressEntity)

  @Query("DELETE FROM test_progress WHERE testId = :testId")
  suspend fun deleteTestProgress(testId: String)

  @Query("DELETE FROM test_progress")
  suspend fun clearAllTestProgress()
}
