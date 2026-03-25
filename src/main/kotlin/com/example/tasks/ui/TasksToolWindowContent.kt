package com.example.tasks.ui

import com.example.tasks.storage.JsonTaskStorage
import com.example.tasks.storage.TaskStorage
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.BoxLayout

/**
 * 任务工具窗口主内容面板
 * 负责初始化存储、构建整体布局，并协调任务列表和添加对话框
 */
class TasksToolWindowContent(private val project: Project) : JPanel(BorderLayout()) {

    /** 任务存储接口实例，使用 JSON 文件存储实现 */
    private val storage: TaskStorage = JsonTaskStorage(project)
    /** 任务列表面板引用，用于刷新 */
    private lateinit var taskListPanel: TaskListPanel

    init {
        // 设置工具窗口默认宽度为 300px
        preferredSize = Dimension(300, 0)
        createUI()
        refreshTasks()
    }

    /**
     * 创建用户界面
     * 整体布局：从上到下依次为 可滚动任务列表 -> 底部添加按钮
     */
    private fun createUI() {
        val mainPanel = JPanel()
        mainPanel.layout = BoxLayout(mainPanel, BoxLayout.Y_AXIS)

        // 任务列表面板（已经包含顶部统计栏）
        taskListPanel = TaskListPanel(storage) {
            refreshTasks()
        }
        val scrollPane = JBScrollPane(taskListPanel)
        mainPanel.add(scrollPane)

        // 底部添加任务按钮，水平居中
        val buttonPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        val addButton = JButton("+ 添加任务")
        addButton.addActionListener {
            showAddTaskDialog()
        }
        buttonPanel.add(addButton)
        mainPanel.add(buttonPanel)

        add(mainPanel, BorderLayout.CENTER)
    }

    /**
     * 刷新任务列表
     * 从存储重新加载数据并更新 UI
     */
    private fun refreshTasks() {
        taskListPanel.refresh()
    }

    /**
     * 显示添加新任务对话框
     * 如果用户确认添加，则将新任务保存到存储并刷新列表
     */
    private fun showAddTaskDialog() {
        val dialog = AddTaskDialog(project, null)
        if (dialog.showAndGet()) {
            val newTask = dialog.getResult()
            storage.addTask(newTask)
            refreshTasks()
        }
    }
}
