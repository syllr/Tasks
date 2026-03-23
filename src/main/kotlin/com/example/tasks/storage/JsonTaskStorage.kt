package com.example.tasks.storage

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.intellij.openapi.project.Project
import java.io.File
import java.time.Instant

private data class StorageData(
    val version: Int = 1,
    val tasks: List<TaskJson> = emptyList()
)

private data class TaskJson(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)

class JsonTaskStorage(private val project: Project) : TaskStorage {

    private val storageFile: File
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var cachedTasks: MutableList<Task> = mutableListOf()

    init {
        val projectBasePath = project.basePath
        if (projectBasePath != null) {
            val ideaDir = File(projectBasePath, ".idea")
            if (!ideaDir.exists()) {
                ideaDir.mkdirs()
            }
            storageFile = File(ideaDir, "tasks.json")
        } else {
            // Default to project root if basePath is null
            storageFile = File(project.name + "-tasks.json")
        }
    }

    override fun loadTasks(): List<Task> {
        cachedTasks = mutableListOf()
        if (storageFile.exists()) {
            try {
                val json = storageFile.readText()
                val data = gson.fromJson(json, StorageData::class.java)
                cachedTasks = data.tasks.map { taskJson ->
                    Task(
                        id = taskJson.id,
                        title = taskJson.title,
                        description = taskJson.description,
                        status = TaskStatus.valueOf(taskJson.status),
                        createdAt = Instant.parse(taskJson.createdAt),
                        updatedAt = Instant.parse(taskJson.updatedAt)
                    )
                }.toMutableList()
            } catch (e: Exception) {
                cachedTasks = mutableListOf()
            }
        }
        return cachedTasks.toList()
    }

    override fun saveTasks(tasks: List<Task>) {
        cachedTasks = tasks.toMutableList()
        doSave()
    }

    override fun addTask(task: Task) {
        cachedTasks.add(task)
        doSave()
    }

    override fun updateTask(task: Task) {
        val index = cachedTasks.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            cachedTasks[index] = task
            doSave()
        }
    }

    override fun deleteTask(taskId: String) {
        cachedTasks.removeAll { it.id == taskId }
        doSave()
    }

    override fun getTaskById(taskId: String): Task? {
        return cachedTasks.find { it.id == taskId }
    }

    private fun doSave() {
        val data = StorageData(
            version = 1,
            tasks = cachedTasks.map { task ->
                TaskJson(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    status = task.status.name,
                    createdAt = task.createdAt.toString(),
                    updatedAt = task.updatedAt.toString()
                )
            }
        )
        storageFile.writeText(gson.toJson(data))
    }
}
