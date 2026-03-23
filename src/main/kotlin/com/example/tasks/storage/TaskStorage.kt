package com.example.tasks.storage

import com.example.tasks.model.Task

interface TaskStorage {
    fun loadTasks(): List<Task>
    fun saveTasks(tasks: List<Task>)
    fun addTask(task: Task)
    fun updateTask(task: Task)
    fun deleteTask(taskId: String)
    fun getTaskById(taskId: String): Task?
}
