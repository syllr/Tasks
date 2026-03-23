package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.example.tasks.storage.TaskStorage
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBBox
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class TaskListPanel(
    private val storage: TaskStorage,
    private val onTaskChanged: () -> Unit
) : JBBox(BoxLayout.Y_AXIS) {

    private val tasks = mutableListOf<Task>()
    private var currentFilter: FilterOption = FilterOption.ALL

    private enum class FilterOption(val displayName: String, val status: TaskStatus?) {
        ALL("全部", null),
        TODO("待办", TaskStatus.TODO),
        IN_PROGRESS("进行中", TaskStatus.IN_PROGRESS),
        DONE("完成", TaskStatus.DONE)
    }

    init {
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        background = JBColor.PanelBackground
        isOpaque = true
    }

    fun refresh() {
        removeAll()
        tasks.clear()
        tasks.addAll(storage.loadTasks())

        // Sort by created time descending: newest tasks first at top
        tasks.sortByDescending { it.createdAt }

        // Top bar: left filter comboBox + right statistics
        add(createTopBar())

        // Add spacing between top bar and first task (same background color, no gray line)
        val topSpacing = JPanel()
        topSpacing.background = background
        topSpacing.preferredSize = Dimension(0, 8)
        add(topSpacing)

        val filteredTasks = if (currentFilter.status == null) {
            tasks
        } else {
            tasks.filter { it.status == currentFilter.status }
        }

        // Add tasks with spacing between them (each task has its own border)
        for (task in filteredTasks) {
            val component = TaskItemComponent(
                task = task,
                onStatusChange = { updatedTask ->
                    storage.updateTask(updatedTask)
                    onTaskChanged()
                    refresh()
                },
                onEdit = { existingTask ->
                    val window = this.topLevelAncestor
                    val project = com.intellij.openapi.project.ProjectManager.getInstance().openProjects.firstOrNull {
                        it.name == existingTask.title // this is just a fallback, shouldn't happen
                    } ?: com.intellij.openapi.project.ProjectManager.getInstance().defaultProject
                    val dialog = AddTaskDialog(project, existingTask)
                    if (dialog.showAndGet()) {
                        val editedTask = dialog.getResult()
                        storage.updateTask(editedTask)
                        onTaskChanged()
                        refresh()
                    }
                },
                onDelete = { taskToDelete ->
                    storage.deleteTask(taskToDelete.id)
                    onTaskChanged()
                    refresh()
                }
            )
            add(component)
            // Add some vertical spacing between task cards
            val spacing = JPanel()
            spacing.background = background
            spacing.preferredSize = Dimension(0, 8)
            add(spacing)
        }

        // Add vertical glue to fill remaining empty space
        add(JBBox.createVerticalGlue())

        revalidate()
        repaint()
    }

    /**
     * 创建顶部栏：左筛选 + 右统计
     *
     * 布局逻辑：
     * 1. 外层用 GridLayout(1, 2) → 强制分成左右两个格子，宽度各占 50%，高度完全相同
     * 2. 每个格子内部结构完全对称，保证视觉对齐：
     *    - 格子本身用 BorderLayout → CENTER 区域用来放内容盒子
     *    - 内容盒子用 JBBox(Y_AXIS) 垂直排列 → 顶部放 vertical glue + 内容 + 底部 vertical glue
     *    - vertical glue（胶水）会自动吸收多余空间，把内容"挤"到正中间，实现强制垂直居中
     * 3. 水平方向：左边内容用 FlowLayout.CENTER → 文字和下拉框水平居中；右边统计文字也设置居中
     */
    private fun createTopBar(): JPanel {
        // 外层面板：分成 1行2列，水平间距 10px，垂直间距 0
        // GridLayout 会强制两个格子宽度、高度完全相等，这是保证左右对齐的关键
        val panel = JPanel(java.awt.GridLayout(1, 2, 10, 0))
        // 四周留 8px 内边距
        panel.border = BorderFactory.createEmptyBorder(8, 8, 8, 8)
        panel.background = JBColor.PanelBackground

        // ========== 左边格子：状态筛选下拉框 ==========
        val leftCell = JPanel(BorderLayout())
        leftCell.background = JBColor.PanelBackground

        // 垂直盒子 + 上下胶水 → 强制把内容挤到垂直中间
        val leftBox = JBBox(BoxLayout.Y_AXIS)  // Y_AXIS = 垂直排列
        leftBox.background = JBColor.PanelBackground
        leftBox.isOpaque = false  // 不自己绘制背景，让父格子绘制
        leftBox.add(JBBox.createVerticalGlue())  // 顶部胶水：吸收顶部多余空间

        // 内容面板："筛选:" + 下拉框 → 水平并排，水平居中
        val leftContent = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0))
        leftContent.background = JBColor.PanelBackground
        leftContent.add(JLabel("筛选: "))
        val filterOptions = FilterOption.entries.toTypedArray()
        val comboBox = JComboBox(filterOptions.map { it.displayName }.toTypedArray())
        comboBox.selectedIndex = FilterOption.entries.indexOf(currentFilter)
        comboBox.addActionListener {
            val selectedIndex = comboBox.selectedIndex
            currentFilter = FilterOption.entries[selectedIndex]
            refresh()  // 筛选变化后刷新任务列表
        }
        leftContent.add(comboBox)

        leftBox.add(leftContent)          // 中间放实际内容
        leftBox.add(JBBox.createVerticalGlue())  // 底部胶水：吸收底部多余空间

        // 把盒子放到格子的 CENTER 区域 → 盒子会自动居中
        leftCell.add(leftBox, BorderLayout.CENTER)
        panel.add(leftCell)

        // ========== 右边格子：任务统计信息 ==========
        val rightCell = JPanel(BorderLayout())
        rightCell.background = JBColor.PanelBackground

        // 和左边完全对称：同样用垂直盒子 + 上下胶水保证垂直居中
        val rightBox = JBBox(BoxLayout.Y_AXIS)
        rightBox.background = JBColor.PanelBackground
        rightBox.isOpaque = false
        rightBox.add(JBBox.createVerticalGlue())

        // 计算各状态任务数量
        val todoCount = tasks.count { it.status == TaskStatus.TODO }
        val inProgressCount = tasks.count { it.status == TaskStatus.IN_PROGRESS }
        val doneCount = tasks.count { it.status == TaskStatus.DONE }

        // 用 HTML 染色：不同状态用不同颜色显示，统计数字加粗
        val label = JLabel("<html>" +
                "<span style='color:#CCCCCC;padding: 0 12px'>待办: <b>$todoCount</b></span> | " +
                "<span style='color:#4285F4;padding: 0 12px'>进行中: <b>$inProgressCount</b></span> | " +
                "<span style='color:#34A853;padding: 0 12px'>完成: <b>$doneCount</b></span>" +
                "</html>")
        label.horizontalAlignment = JLabel.CENTER  // 文字水平居中

        rightBox.add(label)
        rightBox.add(JBBox.createVerticalGlue())

        rightCell.add(rightBox, BorderLayout.CENTER)
        panel.add(rightCell)

        return panel
    }

    private fun createSeparator(): JPanel {
        val separator = JPanel()
        separator.background = Color(0x303030)
        separator.preferredSize = java.awt.Dimension(0, 1)
        separator.alignmentX = CENTER_ALIGNMENT
        return separator
    }
}
