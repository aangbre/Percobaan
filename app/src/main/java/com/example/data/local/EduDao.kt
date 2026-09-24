package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EduDao {
    // Puzzle queries
    @Query("SELECT * FROM puzzles ORDER BY createdAt DESC")
    fun getAllPuzzles(): Flow<List<PuzzleEntity>>

    @Query("SELECT * FROM puzzles WHERE id = :id LIMIT 1")
    suspend fun getPuzzleById(id: String): PuzzleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPuzzle(puzzle: PuzzleEntity)

    @Query("DELETE FROM puzzles WHERE id = :id")
    suspend fun deletePuzzle(id: String)

    // Student Submissions queries
    @Query("SELECT * FROM student_submissions ORDER BY timestamp DESC")
    fun getAllSubmissions(): Flow<List<StudentSubmissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: StudentSubmissionEntity): Long

    @Query("DELETE FROM student_submissions WHERE id = :id")
    suspend fun deleteSubmission(id: Long)

    @Query("DELETE FROM student_submissions")
    suspend fun clearAllSubmissions()
}
