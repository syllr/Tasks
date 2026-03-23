package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Dimension
import java.time.Instant
import java.util.UUID
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane

class AddTaskDialog(
    private val project: Project,
    private val existingTask: Task?
) : DialogWrapper(project, true) {

    private lateinit var titleField: JBTextField
    private lateinit var descriptionArea: JBTextArea

    init {
        title = if (existingTask == null) "添加新任务" else "编辑任务"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(400, 250)

        // Title field
        val titlePanel = JPanel(BorderLayout())
        titlePanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        titlePanel.add(JBLabel("任务标题:"), BorderLayout.NORTH)
        titleField = JBTextField()
        titlePanel.add(titleField, BorderLayout.CENTER)
        panel.add(titlePanel, BorderLayout.NORTH)

        // Description area
        val descriptionPanel = JPanel(BorderLayout())
        descriptionPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        descriptionPanel.add(JBLabel("任务描述:"), BorderLayout.NORTH)
        descriptionArea = JBTextArea(5, 40)
        descriptionArea.lineWrap = true
        descriptionArea.wrapStyleWord = true
        val scrollPane = JScrollPane(descriptionArea)
        descriptionPanel.add(scrollPane, BorderLayout.CENTER)
        panel.add(descriptionPanel, BorderLayout.CENTER)

        // Pre-fill if editing
        existingTask?.let {
            titleField.text = it.title
            descriptionArea.text = it.description
        }

        return panel
    }

    fun getResult(): Task {
        val now = Instant.now()
        return if (existingTask == null) {
            Task(
                id = UUID.randomUUID().toString(),
                title = titleField.text.trim(),
                description = descriptionArea.text.trim(),
                status = TaskStatus.TODO,
                createdAt = now,
                updatedAt = now
            )
        } else {
            existingTask.copy(
                title = titleField.text.trim(),
                description = descriptionArea.text.trim(),
                updatedAt = now
            )
        }
    }

    override fun getInitialFocus(): JComponent {
        return titleField
    }
}
