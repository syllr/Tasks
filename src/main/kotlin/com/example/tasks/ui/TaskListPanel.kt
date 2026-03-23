package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.storage.TaskStorage
import com.intellij.ui.components.JBBox
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JPanel

class TaskListPanel(
    private val storage: TaskStorage,
    private val onTaskChanged: () -> Unit
) : JBBox(VERTICAL) {

    private val tasks = mutableListOf<Task>()

    init {
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    }

    fun refresh() {
        removeAll()
        tasks.clear()
        tasks.addAll(storage.loadTasks())

        for (task in tasks) {
            val component = TaskItemComponent(
                task = task,
                onStatusChange = { updatedTask ->
                    storage.updateTask(updatedTask)
                    onTaskChanged()
                },
                onEdit = { existingTask ->
                    val window = this.topLevelAncestor
                    val project = com.intellij.openapi.project.ProjectManager.getInstance().openProjects.firstOrNull {
                        it.name == existingTask.title // this is just a fallback, shouldn't happen
                    } ?: com.intellij.openapi.project.ProjectManager.getInstance().defaultProject
                    val dialog = AddTaskDialog(project, existingTask)
                    if (dialog.showAndGet()) {
                        val editedTask = dialog.getResult()
                        storage.updateTask(editedTask)
                        onTaskChanged()
                    }
                },
                onDelete = { taskToDelete ->
                    storage.deleteTask(taskToDelete.id)
                    onTaskChanged()
                }
            )
            add(component)
            add(createSeparator())
        }

        revalidate()
        repaint()
    }

    private fun createSeparator(): JPanel {
        val separator = JPanel()
        separator.background = Color(0x303030)
        separator.height = 1
        separator.alignmentX = CENTER_ALIGNMENT
        return separator
    }
}
