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
    val updatedAt: String,
    val isProjectLevel: Boolean
)

class JsonTaskStorage(private val project: Project) : TaskStorage {

    private val projectStorageFile: File?
    private val userStorageFile: File?
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private var cachedTasks: MutableList<Task> = mutableListOf()

    init {
        val projectBasePath = project.basePath
        val projectName = project.name

        // Project-level storage: .idea/tasks.json inside project
        projectStorageFile = if (projectBasePath != null) {
            val ideaDir = File(projectBasePath, ".idea")
            if (!ideaDir.exists()) {
                ideaDir.mkdirs()
            }
            File(ideaDir, "tasks.json")
        } else {
            null
        }

        // User-level storage: ~/.todoTasks/{project-name}.json
        val userHome = System.getProperty("user.home")
        val todoTasksDir = File(userHome, ".todoTasks")
        if (!todoTasksDir.exists()) {
            todoTasksDir.mkdirs()
        }
        userStorageFile = File(todoTasksDir, "${projectName}.json")
    }

    override fun loadTasks(): List<Task> {
        cachedTasks = mutableListOf()

        // Load project-level tasks
        projectStorageFile?.let { file ->
            if (file.exists()) {
                try {
                    val json = file.readText()
                    val data = gson.fromJson(json, StorageData::class.java)
                    cachedTasks.addAll(data.tasks.map { taskJson ->
                        Task(
                            id = taskJson.id,
                            title = taskJson.title,
                            description = taskJson.description,
                            status = TaskStatus.valueOf(taskJson.status),
                            createdAt = Instant.parse(taskJson.createdAt),
                            updatedAt = Instant.parse(taskJson.updatedAt),
                            isProjectLevel = taskJson.isProjectLevel
                        )
                    })
                } catch (e: Exception) {
                    // Ignore errors
                }
            }
        }

        // Load user-level tasks
        userStorageFile?.let { file ->
            if (file.exists()) {
                try {
                    val json = file.readText()
                    val data = gson.fromJson(json, StorageData::class.java)
                    cachedTasks.addAll(data.tasks.map { taskJson ->
                        Task(
                            id = taskJson.id,
                            title = taskJson.title,
                            description = taskJson.description,
                            status = TaskStatus.valueOf(taskJson.status),
                            createdAt = Instant.parse(taskJson.createdAt),
                            updatedAt = Instant.parse(taskJson.updatedAt),
                            isProjectLevel = taskJson.isProjectLevel
                        )
                    })
                } catch (e: Exception) {
                    // Ignore errors
                }
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
        // Split tasks by level and save to respective files
        val projectTasks = cachedTasks.filter { it.isProjectLevel }
        val userTasks = cachedTasks.filter { !it.isProjectLevel }

        projectStorageFile?.let { file ->
            val data = StorageData(
                version = 1,
                tasks = projectTasks.map { task ->
                    TaskJson(
                        id = task.id,
                        title = task.title,
                        description = task.description,
                        status = task.status.name,
                        createdAt = task.createdAt.toString(),
                        updatedAt = task.updatedAt.toString(),
                        isProjectLevel = task.isProjectLevel
                    )
                }
            )
            file.writeText(gson.toJson(data))
        }

        userStorageFile?.let { file ->
            val data = StorageData(
                version = 1,
                tasks = userTasks.map { task ->
                    TaskJson(
                        id = task.id,
                        title = task.title,
                        description = task.description,
                        status = task.status.name,
                        createdAt = task.createdAt.toString(),
                        updatedAt = task.updatedAt.toString(),
                        isProjectLevel = task.isProjectLevel
                    )
                }
            )
            file.writeText(gson.toJson(data))
        }
    }
}
