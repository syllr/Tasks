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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.swing.*

/**
 * 单个任务卡片组件
 * 显示任务标题、描述、状态按钮，并提供编辑、删除操作
 * 点击卡片任意位置（非按钮）弹出详情弹窗
 * @param task 任务数据
 * @param onStatusChange 状态变更回调
 * @param onEdit 编辑回调
 * @param onDelete 删除回调
 */
class TaskItemComponent(
    val task: Task,
    private val onStatusChange: (Task) -> Unit,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : JPanel(BorderLayout()) {

    init {
        // 固定高度 70px，宽度填满容器
        preferredSize = Dimension(0, 70)
        minimumSize = Dimension(0, 70)
        maximumSize = Dimension(Int.MAX_VALUE, 70)
        // 复合边框：圆角线边框 + 内边距
        border = BorderFactory.createCompoundBorder(
            com.intellij.ui.RoundedLineBorder(JBColor.GRAY, 5, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )
        background = JBColor.PanelBackground
        isOpaque = true

        // ========== 左侧：标题和描述区域 ==========
        val textPanel = JPanel(BorderLayout())
        textPanel.border = BorderFactory.createEmptyBorder(4, 8, 4, 8)
        textPanel.background = background
        textPanel.isOpaque = false

        // 构建标题，用户级任务标注 [用户]
        val titleText = buildString {
            append(task.title)
            if (!task.isProjectLevel) {
                append(" <small>[用户]</small>")
            }
        }
        // 使用 HTML 支持富文本显示
        val titleLabel = JLabel("<html>$titleText</html>")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD)
        // 已完成任务：灰色 + 删除线效果
        if (task.status == TaskStatus.DONE) {
            titleLabel.foreground = JBColor.GRAY
            titleLabel.text = "<html><strike>$titleText</strike></html>"
        }
        textPanel.add(titleLabel, BorderLayout.NORTH)

        // 如果有描述，添加描述标签
        if (!task.description.isNullOrBlank()) {
            val descriptionLabel = JLabel(task.description)
            descriptionLabel.font = descriptionLabel.font.deriveFont(Font.PLAIN, 11f)
            descriptionLabel.foreground = JBColor.GRAY
            textPanel.add(descriptionLabel, BorderLayout.CENTER)
        }

        add(textPanel, BorderLayout.CENTER)

        // ========== 右侧：状态按钮 + 编辑 + 删除 ==========
        // 水平排列三个按钮
        val buttonsPanel = JPanel(FlowLayout(FlowLayout.CENTER, 4, 0))
        buttonsPanel.background = background
        buttonsPanel.isOpaque = false

        // 状态按钮：点击切换到下一状态，颜色区分不同状态
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

        // 编辑按钮
        val editButton = JButton("编辑")
        editButton.preferredSize = Dimension(50, 28)
        editButton.toolTipText = "编辑任务"
        editButton.addActionListener { onEdit(task) }
        buttonsPanel.add(editButton)

        // 删除按钮
        val deleteButton = JButton("删除")
        deleteButton.preferredSize = Dimension(50, 28)
        deleteButton.toolTipText = "删除任务"
        deleteButton.addActionListener { onDelete(task) }
        buttonsPanel.add(deleteButton)

        // 使用上下 vertical glue 让按钮面板垂直居中
        val buttonsBox = JBBox(BoxLayout.Y_AXIS)
        buttonsBox.background = background
        buttonsBox.isOpaque = false
        buttonsBox.add(JBBox.createVerticalGlue())
        buttonsBox.add(buttonsPanel)
        buttonsBox.add(JBBox.createVerticalGlue())
        buttonsBox.preferredSize = Dimension(190, 0)
        add(buttonsBox, BorderLayout.EAST)

        // 点击卡片任意位置（非按钮）弹出详情弹窗
        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (e.source !is JButton) {
                    showDetailsPopup()
                }
            }
        })
    }

    /**
     * 显示任务详情弹窗
     * 使用 IntelliJ 原生 JBPopup 组件，支持拖动和调整大小
     * 显示完整标题、描述、创建时间
     */
    private fun showDetailsPopup() {
        val panel = JPanel(BorderLayout())
        panel.border = BorderFactory.createEmptyBorder(12, 12, 12, 12)
        panel.preferredSize = Dimension(400, 200)

        // 标题区域
        val titlePanel = JPanel(BorderLayout())
        titlePanel.border = BorderFactory.createEmptyBorder(0, 0, 8, 0)
        val titleLabel = JBLabel("任务标题: ${task.title}")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, 14f)
        titlePanel.add(titleLabel, BorderLayout.CENTER)
        panel.add(titlePanel, BorderLayout.NORTH)

        // 描述区域
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

        // 创建时间
        val formattedTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .format(task.createdAt.atZone(ZoneId.systemDefault()))
        val timeLabel = JBLabel("创建时间: $formattedTime")
        timeLabel.foreground = JBColor.GRAY
        timeLabel.font = timeLabel.font.deriveFont(Font.PLAIN, 11f)
        panel.add(timeLabel, BorderLayout.SOUTH)

        // 使用 JBPopupFactory 创建原生弹窗
        val popup = JBPopupFactory.getInstance()
            .createComponentPopupBuilder(panel, null)
            .setTitle("任务详情")
            .setMovable(true)
            .setResizable(true)
            .createPopup()

        popup.showUnderneathOf(this)
    }

    /**
     * 根据状态获取按钮显示文字
     */
    private fun getStatusText(): String {
        return when (task.status) {
            TaskStatus.TODO -> task.status.displayName
            TaskStatus.IN_PROGRESS -> task.status.displayName
            TaskStatus.DONE -> "完成"
        }
    }

    /**
     * 根据状态获取按钮背景颜色
     * 待办：灰色 / 进行中：蓝色 / 完成：绿色
     * 使用 JBColor 支持浅色/深色主题自适应
     */
    private fun getStatusColor(): JBColor {
        return when (task.status) {
            TaskStatus.TODO -> JBColor(0xCCCCCC, 0x555555)
            TaskStatus.IN_PROGRESS -> JBColor(0x4285F4, 0x4285F4)
            TaskStatus.DONE -> JBColor(0x34A853, 0x34A853)
        }
    }
}
