package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.storage.JsonTaskStorage
import com.example.tasks.storage.TaskStorage
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBBox
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel
import javax.swing.JButton

class TasksToolWindowContent(private val project: Project) : JPanel(BorderLayout()) {

    private val storage: TaskStorage = JsonTaskStorage(project)
    private lateinit var taskListPanel: TaskListPanel

    init {
        preferredSize = Dimension(300, 0)
        createUI()
        refreshTasks()
    }

    private fun createUI() {
        // Toolbar with add button
        val toolbarPanel = JPanel(BorderLayout())
        val addButton = JButton("+ 添加任务")
        addButton.addActionListener {
            showAddTaskDialog()
        }
        toolbarPanel.add(addButton, BorderLayout.WEST)
        add(toolbarPanel, BorderLayout.NORTH)

        // Task list panel
        taskListPanel = TaskListPanel(storage) {
            refreshTasks()
        }
        val scrollPane = JBScrollPane(taskListPanel)
        add(scrollPane, BorderLayout.CENTER)
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
