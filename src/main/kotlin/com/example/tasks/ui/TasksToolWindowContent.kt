package com.example.tasks.ui

import com.example.tasks.storage.JsonTaskStorage
import com.example.tasks.storage.TaskStorage
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBBox
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.BoxLayout

class TasksToolWindowContent(private val project: Project) : JPanel(BorderLayout()) {

    private val storage: TaskStorage = JsonTaskStorage(project)
    private lateinit var taskListPanel: TaskListPanel

    init {
        preferredSize = Dimension(300, 0)
        createUI()
        refreshTasks()
    }

    private fun createUI() {
        // Overall layout: top to bottom - [statistics] -> [task list scroll] -> [add button]
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)

        // Task list panel (already includes statistics bar at the top)
        taskListPanel = TaskListPanel(storage) {
            refreshTasks()
        }
        val scrollPane = JBScrollPane(taskListPanel)
        mainPanel.add(scrollPane)

        // Add button at the bottom
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        val addButton = JButton("+ 添加任务")
        addButton.addActionListener {
            showAddTaskDialog()
        }
        buttonPanel.add(addButton)
        mainPanel.add(buttonPanel)

        add(mainPanel, BorderLayout.CENTER)
    }

    private fun refreshTasks() {
        taskListPanel.refresh()
    }

    private fun showAddTaskDialog() {
        val dialog = AddTaskDialog(project, null)
        if (dialog.showAndGet()) {
            val newTask = dialog.getResult()
            storage.addTask(newTask)
            refreshTasks()
        }
    }
}
