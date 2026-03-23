package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.example.tasks.storage.TaskStorage
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBBox
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class TaskListPanel(
    private val storage: TaskStorage,
    private val onTaskChanged: () -> Unit
) : JBBox(BoxLayout.Y_AXIS) {

    private val tasks = mutableListOf<Task>()

    init {
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
    }

    fun refresh() {
        removeAll()
        tasks.clear()
        tasks.addAll(storage.loadTasks())

        // Add status statistics bar at the top
        add(createStatisticsBar())
        add(createSeparator())

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

    private fun createStatisticsBar(): JPanel {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        panel.background = JBColor.PanelBackground

        val todoCount = tasks.count { it.status == TaskStatus.TODO }
        val inProgressCount = tasks.count { it.status == TaskStatus.IN_PROGRESS }
        val doneCount = tasks.count { it.status == TaskStatus.DONE }

        val label = JLabel("<html>" +
                "<span style='color:#CCCCCC;padding: 0 12px'>待办: <b>$todoCount</b></span> | " +
                "<span style='color:#4285F4;padding: 0 12px'>进行中: <b>$inProgressCount</b></span> | " +
                "<span style='color:#34A853;padding: 0 12px'>已完成: <b>$doneCount</b></span>" +
                "</html>")
        label.horizontalAlignment = JLabel.CENTER

        panel.add(label, BorderLayout.CENTER)
        return panel
    }

    private fun createSeparator(): JPanel {
        val separator = JPanel()
        separator.background = Color(0x303030)
        separator.preferredSize = java.awt.Dimension(0, 1)
        separator.alignmentX = CENTER_ALIGNMENT
        return separator
    }
}
