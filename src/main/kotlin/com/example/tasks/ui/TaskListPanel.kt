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
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
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

        // Top bar: left filter + right statistics
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
     * 1. 外层 GridLayout(1, 2) → 强制左右格子宽度相等，高度相同
     * 2. 每个格子内部用 GridBagLayout → anchor=BASELINE 保证文字基线对齐
     * 3. 水平方向都居中，所以"筛选"和"待办"两个词绝对在同一水平线上
     */
    private fun createTopBar(): JPanel {
        // 外层面板：分成 1行2列，水平间距 10px
        // GridLayout 强制两个格子宽度、高度完全相等
        val panel = JPanel(java.awt.GridLayout(1, 2, 10, 0))
        panel.border = BorderFactory.createEmptyBorder(1, 8, 1, 8)  // 减小上下内边距
        panel.background = JBColor.PanelBackground

        // ========== 左边格子：状态筛选下拉框 ==========
        val leftCell = JPanel(GridBagLayout())
        leftCell.background = JBColor.PanelBackground

        val gcLeft = GridBagConstraints()
        // BASELINE 本身就是：水平居中 + 垂直基线对齐（正好就是我们要的）
        gcLeft.anchor = GridBagConstraints.BASELINE
        gcLeft.weightx = 1.0  // 水平方向占满格子，保证水平居中
        gcLeft.weighty = 0.0  // 垂直方向只需要自己的高度，不要扩展

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

        leftCell.add(leftContent, gcLeft)
        panel.add(leftCell)

        // ========== 右边格子：任务统计信息 ==========
        val rightCell = JPanel(GridBagLayout())
        rightCell.background = JBColor.PanelBackground

        val gcRight = GridBagConstraints()
        // 同样：水平居中 + 垂直基线对齐
        gcRight.anchor = GridBagConstraints.BASELINE
        gcRight.weightx = 1.0  // 水平方向占满格子，保证水平居中
        gcRight.weighty = 0.0  // 垂直方向只需要自己的高度，不要扩展

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
        label.horizontalAlignment = JLabel.CENTER

        rightCell.add(label, gcRight)
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
