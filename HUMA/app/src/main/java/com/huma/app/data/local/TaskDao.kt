package com.huma.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    // ===== CRUD =====
    @Insert
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // ===== QUERY =====
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    // 🔥 GET TASK BY ID (INI YANG HILANG)
    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    fun getTaskById(taskId: Int): Flow<TaskEntity?>

    // (optional, boleh kepake / boleh enggak)
    @Query("""
        SELECT * FROM tasks
        WHERE startDate <= :todayEnd
        AND (deadlineDate IS NULL OR deadlineDate >= :todayStart)
        AND isDone = 0
        ORDER BY priority DESC
    """)
    fun getTodayTasks(
        todayStart: Long,
        todayEnd: Long
    ): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks
        WHERE startDate > :todayEnd
        ORDER BY startDate ASC
    """)
    fun getUpcomingTasks(todayEnd: Long): Flow<List<TaskEntity>>
}
