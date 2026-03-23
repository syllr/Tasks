package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBBox
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea

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
        // Compound border: rounded line border + inner padding
        border = BorderFactory.createCompoundBorder(
            com.intellij.ui.RoundedLineBorder(JBColor.GRAY, 5, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )
        background = com.intellij.ui.JBColor.PanelBackground
        isOpaque = true

        // Left: title and description
        val textPanel = JPanel(BorderLayout())
        textPanel.border = BorderFactory.createEmptyBorder(4, 8, 4, 8)
        textPanel.background = background
        textPanel.isOpaque = false

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

        // Right: status button + edit + delete - all horizontal, same style
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 4, 0))
        buttonsPanel.background = background
        buttonsPanel.isOpaque = false

        val statusButton = JButton(getStatusText())
        statusButton.background = getStatusColor()
        statusButton.foreground = JBColor.WHITE
        statusButton.preferredSize = Dimension(70, 28)
        statusButton.isContentAreaFilled = true
        statusButton.setBorderPainted(true)
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
        buttonsBox.background = background
        buttonsBox.isOpaque = false
        buttonsBox.add(JBBox.createVerticalGlue())
        buttonsBox.add(buttonsPanel)
        buttonsBox.add(JBBox.createVerticalGlue())
        buttonsBox.preferredSize = Dimension(190, 0)
        add(buttonsBox, BorderLayout.EAST)

        // Click anywhere on the task card to show details popup
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (e.source !is JButton) {
                    showDetailsPopup()
                }
            }
        })
    }

    private fun showDetailsPopup() {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
        panel.preferredSize = Dimension(400, 200)

        // Title
        val titlePanel = JPanel(BorderLayout())
        titlePanel.border = BorderFactory.createEmptyBorder(0, 0, 8, 0)
        val titleLabel = JBLabel("任务标题: ${task.title}")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 14f)
        titlePanel.add(titleLabel, BorderLayout.CENTER)
        panel.add(titlePanel, BorderLayout.NORTH)

        // Description
        val descriptionPanel = JPanel(BorderLayout())
        descriptionPanel.border = BorderFactory.createEmptyBorder(0, 0, 8, 0)
        val descriptionText = if (task.description.isNullOrBlank()) {
            "(无描述)"
        } else {
            task.description
        }
        val descriptionArea = JTextArea(descriptionText)
        descriptionArea.font = descriptionArea.font.deriveFont(Font.PLAIN, 12f)
        descriptionArea.isEditable = false
        descriptionArea.lineWrap = true
        descriptionArea.wrapStyleWord = true
        descriptionArea.background = null
        descriptionPanel.add(descriptionArea, BorderLayout.CENTER)
        panel.add(descriptionPanel, BorderLayout.CENTER)

        // Created time
        val formattedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .format(task.createdAt.atZone(ZoneId.systemDefault()))
        val timeLabel = JBLabel("创建时间: $formattedTime")
        timeLabel.foreground = JBColor.GRAY
        timeLabel.font = timeLabel.font.deriveFont(Font.PLAIN, 11f)
        panel.add(timeLabel, BorderLayout.SOUTH)

        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, null)
            .setTitle("任务详情")
            .setMovable(true)
            .setResizable(true)
            .createPopup()

        popup.showUnderneathOf(this)
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
