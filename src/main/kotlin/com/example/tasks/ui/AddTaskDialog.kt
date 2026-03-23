package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.Panel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.time.Instant
import java.util.UUID
import javax.swing.BorderFactory
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane

class AddTaskDialog(
    private val project: Project,
    private val existingTask: Task?
) : DialogWrapper(project, true) {

    private lateinit var titleField: JBTextField
    private lateinit var descriptionArea: JBTextArea
    private lateinit var projectLevelRadio: JRadioButton
    private lateinit var userLevelRadio: JRadioButton

    init {
        title = if (existingTask == null) "添加新任务" else "编辑任务"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(400, 300)

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

        // Storage level selection
        val levelPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        levelPanel.border = BorderFactory.createEmptyBorder(8, 5, 8, 5)
        levelPanel.add(JBLabel("存储位置:"))

        projectLevelRadio = JRadioButton("项目级别（存在 .idea 目录)", existingTask?.isProjectLevel ?: true)
        userLevelRadio = JRadioButton("用户级别（存在 ~/.todoTasks 目录)", existingTask?.isProjectLevel == false)

        val buttonGroup = ButtonGroup()
        buttonGroup.add(projectLevelRadio)
        buttonGroup.add(userLevelRadio)

        levelPanel.add(projectLevelRadio)
        levelPanel.add(userLevelRadio)
        panel.add(levelPanel, BorderLayout.SOUTH)

        // Pre-fill if editing
        existingTask?.let {
            titleField.text = it.title
            descriptionArea.text = it.description
        }

        return panel
    }

    fun getResult(): Task {
        val now = Instant.now()
        val isProjectLevel = projectLevelRadio.isSelected
        return if (existingTask == null) {
            Task(
                id = UUID.randomUUID().toString(),
                title = titleField.text.trim(),
                description = descriptionArea.text.trim(),
                status = TaskStatus.TODO,
                createdAt = now,
                updatedAt = now,
                isProjectLevel = isProjectLevel
            )
        } else {
            existingTask.copy(
                title = titleField.text.trim(),
                description = descriptionArea.text.trim(),
                updatedAt = now,
                isProjectLevel = isProjectLevel
            )
        }
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return titleField
    }
}
