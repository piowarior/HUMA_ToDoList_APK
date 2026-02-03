package com.huma.app.data.repository

import com.huma.app.data.local.TaskDao
import com.huma.app.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {

    fun getAllTasks(): Flow<List<TaskEntity>> =
        taskDao.getAllTasks()

    fun getTaskById(taskId: Int): Flow<TaskEntity?> =
        taskDao.getTaskById(taskId)

    suspend fun insert(task: TaskEntity) =
        taskDao.insertTask(task)

    suspend fun update(task: TaskEntity) =
        taskDao.updateTask(task)

    suspend fun delete(task: TaskEntity) =
        taskDao.deleteTask(task)
}
