package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.intellij.ui.JBColor
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class TaskItemComponent(
    val task: Task,
    private val onStatusChange: (Task) -> Unit,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : JPanel(BorderLayout()) {

    init {
        preferredSize = Dimension(0, 60)
        minimumSize = Dimension(0, 60)
        maximumSize = Dimension(Int.MAX_VALUE, 60)
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        // Status button
        val statusButton = JButton(getStatusText())
        statusButton.background = getStatusColor()
        statusButton.foreground = JBColor.WHITE
        statusButton.addActionListener {
            val newStatus = task.status.next()
            val updatedTask = task.copy(status = newStatus)
            onStatusChange(updatedTask)
        }
        add(statusButton, BorderLayout.WEST)

        // Title and description
        val textPanel = JPanel(BorderLayout())
        textPanel.border = BorderFactory.createEmptyBorder(0, 8, 0, 8)

        val titleText = buildString {
            append(task.title)
            if (!task.isProjectLevel) {
                append(" <small>[用户]</small>")
            }
        }
        val titleLabel = JLabel("<html>$titleText</html>")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD)
        if (task.status == TaskStatus.DONE) {
            titleLabel.foreground = JBColor.GRAY
            titleLabel.text = "<html><strike>$titleText</strike></html>"
        }
        textPanel.add(titleLabel, BorderLayout.NORTH)

        if (!task.description.isNullOrBlank()) {
            val descriptionLabel = JLabel(task.description)
            descriptionLabel.font = descriptionLabel.font.deriveFont(Font.PLAIN, 11f)
            descriptionLabel.foreground = JBColor.GRAY
            textPanel.add(descriptionLabel, BorderLayout.CENTER)
        }

        add(textPanel, BorderLayout.CENTER)

        // Action buttons (edit and delete)
        val actionsPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 4, 0))
        val editButton = JButton("编辑")
        editButton.preferredSize = Dimension(50, 28)
        editButton.toolTipText = "编辑任务"
        editButton.addActionListener { onEdit(task) }
        actionsPanel.add(editButton)

        val deleteButton = JButton("删除")
        deleteButton.preferredSize = Dimension(50, 28)
        deleteButton.toolTipText = "删除任务"
        deleteButton.addActionListener { onDelete(task) }
        actionsPanel.add(deleteButton)

        add(actionsPanel, BorderLayout.EAST)
    }

    private fun getStatusText(): String {
        return when (task.status) {
            TaskStatus.TODO -> task.status.displayName
            TaskStatus.IN_PROGRESS -> task.status.displayName
            TaskStatus.DONE -> task.status.displayName
        }
    }

    private fun getStatusColor(): JBColor {
        return when (task.status) {
            TaskStatus.TODO -> JBColor(0xCCCCCC, 0x555555)
            TaskStatus.IN_PROGRESS -> JBColor(0x4285F4, 0x4285F4)
            TaskStatus.DONE -> JBColor(0x34A853, 0x34A853)
        }
    }
}
