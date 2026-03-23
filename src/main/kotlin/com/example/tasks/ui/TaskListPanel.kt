package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.example.tasks.storage.TaskStorage
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBBox
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class TaskListPanel(
    private val storage: TaskStorage,
    private val onTaskChanged: () -> Unit
) : JBBox(BoxLayout.Y_AXIS) {

    private val tasks = mutableListOf<Task>()
    private var currentFilter: FilterOption = FilterOption.ALL

    private enum class FilterOption(val displayName: String, val status: TaskStatus?) {
        ALL("全部", null),
        TODO("待办", TaskStatus.TODO),
        IN_PROGRESS("进行中", TaskStatus.IN_PROGRESS),
        DONE("完成", TaskStatus.DONE)
    }

    init {
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        background = JBColor.PanelBackground
        isOpaque = true
    }

    fun refresh() {
        removeAll()
        tasks.clear()
        tasks.addAll(storage.loadTasks())

        // Sort by created time descending: newest tasks first at top
        tasks.sortByDescending { it.createdAt }

        // Top bar: left filter comboBox + right statistics
        add(createTopBar())

        // Add spacing between top bar and first task (same background color, no gray line)
        val topSpacing = JPanel()
        topSpacing.background = background
        topSpacing.preferredSize = Dimension(0, 8)
        add(topSpacing)

        val filteredTasks = if (currentFilter.status == null) {
            tasks
        } else {
            tasks.filter { it.status == currentFilter.status }
        }

        // Add tasks with spacing between them (each task has its own border)
        for (task in filteredTasks) {
            val component = TaskItemComponent(
                task = task,
                onStatusChange = { updatedTask ->
                    storage.updateTask(updatedTask)
                    onTaskChanged()
                    refresh()
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
                        refresh()
                    }
                },
                onDelete = { taskToDelete ->
                    storage.deleteTask(taskToDelete.id)
                    onTaskChanged()
                    refresh()
                }
            )
            add(component)
            // Add some vertical spacing between task cards
            val spacing = JPanel()
            spacing.background = background
            spacing.preferredSize = Dimension(0, 8)
            add(spacing)
        }

        // Add vertical glue to fill remaining empty space
        add(JBBox.createVerticalGlue())

        revalidate()
        repaint()
    }

    private fun createTopBar(): JPanel {
        // GridLayout(1, 2) - split into two equal width cells
        val panel = JPanel(java.awt.GridLayout(1, 2, 10, 0))
        panel.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        panel.background = JBColor.PanelBackground

        // Left cell: filter comboBox - content centered both horizontally and vertically
        val leftCell = JPanel(BorderLayout())
        leftCell.background = JBColor.PanelBackground

        // Use nested BorderLayout to guarantee vertical centering for single row
        val leftContainer = JPanel(BorderLayout())
        leftContainer.background = JBColor.PanelBackground

        val leftContent = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0))
        leftContent.background = JBColor.PanelBackground
        leftContent.add(JLabel("筛选: "))
        val filterOptions = FilterOption.entries.toTypedArray()
        val comboBox = JComboBox(filterOptions.map { it.displayName }.toTypedArray())
        comboBox.selectedIndex = FilterOption.entries.indexOf(currentFilter)
        comboBox.addActionListener {
            val selectedIndex = comboBox.selectedIndex
            currentFilter = FilterOption.entries[selectedIndex]
            refresh()
        }
        leftContent.add(comboBox)

        leftContainer.add(leftContent, BorderLayout.CENTER)
        leftCell.add(leftContainer, BorderLayout.CENTER)
        panel.add(leftCell)

        // Right cell: statistics - content centered both horizontally and vertically
        val rightCell = JPanel(BorderLayout())
        rightCell.background = JBColor.PanelBackground

        val todoCount = tasks.count { it.status == TaskStatus.TODO }
        val inProgressCount = tasks.count { it.status == TaskStatus.IN_PROGRESS }
        val doneCount = tasks.count { it.status == TaskStatus.DONE }

        val label = JLabel("<html>" +
                "<span style='color:#CCCCCC;padding: 0 12px'>待办: <b>$todoCount</b></span> | " +
                "<span style='color:#4285F4;padding: 0 12px'>进行中: <b>$inProgressCount</b></span> | " +
                "<span style='color:#34A853;padding: 0 12px'>完成: <b>$doneCount</b></span>" +
                "</html>")
        label.horizontalAlignment = JLabel.CENTER

        rightCell.add(label, BorderLayout.CENTER)
        panel.add(rightCell)

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
