package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBBox
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.BoxLayout
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
        preferredSize = Dimension(0, 70)
        minimumSize = Dimension(0, 70)
        maximumSize = Dimension(Int.MAX_VALUE, 70)
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        // Round border
        background = com.intellij.ui.JBColor.PanelBackground

        // Left: title and description
        val textPanel = JPanel(BorderLayout())
        textPanel.border = BorderFactory.createEmptyBorder(4, 8, 4, 8)

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

        // Right: status button + edit + delete - all horizontal
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 4, 0))
        val statusButton = JButton(getStatusText())
        statusButton.background = getStatusColor()
        statusButton.foreground = JBColor.WHITE
        statusButton.preferredSize = Dimension(70, 28)
        statusButton.addActionListener {
            val newStatus = task.status.next()
            val updatedTask = task.copy(status = newStatus)
            onStatusChange(updatedTask)
        }
        buttonsPanel.add(statusButton)

        val editButton = JButton("编辑")
        editButton.preferredSize = Dimension(50, 28)
        editButton.toolTipText = "编辑任务"
        editButton.addActionListener { onEdit(task) }
        buttonsPanel.add(editButton)

        val deleteButton = JButton("删除")
        deleteButton.preferredSize = Dimension(50, 28)
        deleteButton.toolTipText = "删除任务"
        deleteButton.addActionListener { onDelete(task) }
        buttonsPanel.add(deleteButton)

        // Vertical center the whole buttons panel
        val buttonsBox = JBBox(BoxLayout.Y_AXIS)
        buttonsBox.add(JBBox.createVerticalGlue())
        buttonsBox.add(buttonsPanel)
        buttonsBox.add(JBBox.createVerticalGlue())
        buttonsBox.preferredSize = Dimension(190, 0)
        add(buttonsBox, BorderLayout.EAST)
    }

    private fun getStatusText(): String {
        return when (task.status) {
            TaskStatus.TODO -> task.status.displayName
            TaskStatus.IN_PROGRESS -> task.status.displayName
            TaskStatus.DONE -> "完成"
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
