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

/**
 * 添加/编辑任务对话框
 * 支持新建任务和编辑现有任务，提供表单验证和存储级别选择
 * @param project 当前项目
 * @param existingTask 要编辑的现有任务，如果为 null 表示新建任务
 */
class AddTaskDialog(
    private val project: Project,
    private val existingTask: Task?
) : DialogWrapper(project, true) {

    private lateinit var titleField: JBTextField
    private lateinit var descriptionArea: JBTextArea
    private lateinit var projectLevelRadio: JRadioButton
    private lateinit var userLevelRadio: JRadioButton

    init {
        // 设置对话框标题：新建还是编辑
        title = if (existingTask == null) "添加新任务" else "编辑任务"
        init()
    }

    /**
     * 创建对话框中心面板
     * @return 对话框内容组件
     */
    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(400, 300)

        // ========== 标题输入框 ==========
        val titlePanel = JPanel(BorderLayout())
        titlePanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        titlePanel.add(JBLabel("任务标题:"), BorderLayout.NORTH)
        titleField = JBTextField()
        titlePanel.add(titleField, BorderLayout.CENTER)
        panel.add(titlePanel, BorderLayout.NORTH)

        // ========== 描述文本区域 ==========
        val descriptionPanel = JPanel(BorderLayout())
        descriptionPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        descriptionPanel.add(JBLabel("任务描述:"), BorderLayout.NORTH)
        descriptionArea = JBTextArea(5, 40)
        descriptionArea.lineWrap = true
        descriptionArea.wrapStyleWord = true
        val scrollPane = JScrollPane(descriptionArea)
        descriptionPanel.add(scrollPane, BorderLayout.CENTER)
        panel.add(descriptionPanel, BorderLayout.CENTER)

        // ========== 存储级别选择 ==========
        val levelPanel = JPanel(FlowLayout(FlowLayout.LEFT))
        levelPanel.border = BorderFactory.createEmptyBorder(8, 5, 8, 5)
        levelPanel.add(JBLabel("存储位置:"))

        projectLevelRadio = JRadioButton("项目级别（存在 .idea 目录)", existingTask?.isProjectLevel ?: true)
        userLevelRadio = JRadioButton("用户级别（存在 ~/.todoTasks 目录)", existingTask?.isProjectLevel == false)

        // 单选按钮组，保证只能选一个
        val buttonGroup = ButtonGroup()
        buttonGroup.add(projectLevelRadio)
        buttonGroup.add(userLevelRadio)

        levelPanel.add(projectLevelRadio)
        levelPanel.add(userLevelRadio)
        panel.add(levelPanel, BorderLayout.SOUTH)

        // 如果是编辑，预先填充现有内容
        existingTask?.let {
            titleField.text = it.title
            descriptionArea.text = it.description
        }

        return panel
    }

    /**
     * 获取对话框结果，构建 Task 对象
     * @return 新建或编辑后的 Task 对象
     */
    fun getResult(): Task {
        val now = Instant.now()
        val isProjectLevel = projectLevelRadio.isSelected
        return if (existingTask == null) {
            // 新建任务：生成 UUID，默认状态为待办
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
            // 编辑任务：复用原有 id、状态、创建时间，只更新修改内容和更新时间
            existingTask.copy(
                title = titleField.text.trim(),
                description = descriptionArea.text.trim(),
                updatedAt = now,
                isProjectLevel = isProjectLevel
            )
        }
    }

    /**
     * 获取首选焦点组件
     * @return 标题输入框，打开对话框自动聚焦到标题
     */
    override fun getPreferredFocusedComponent(): JComponent {
        return titleField
    }

    /**
     * 处理 OK 按钮点击
     * 先验证标题不为空，如果为空显示错误信息，不关闭对话框
     */
    override fun doOKAction() {
        val titleText = titleField.text.trim()
        if (titleText.isEmpty()) {
            // 显示错误对话框，保持当前对话框不关闭
            com.intellij.openapi.ui.Messages.showErrorDialog(
                project,
                "任务标题不能为空，请输入标题后再保存",
                "输入错误"
            )
            return
        }
        super.doOKAction()
    }
}
