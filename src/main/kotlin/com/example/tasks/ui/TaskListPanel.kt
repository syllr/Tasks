package com.example.tasks.ui

import com.example.tasks.model.Task
import com.example.tasks.model.TaskStatus
import com.example.tasks.storage.TaskStorage
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBBox
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * 任务列表面板
 * 负责显示筛选栏、任务卡片列表、统计信息，支持筛选刷新
 * @param storage 任务存储接口
 * @param onTaskChanged 任务变更回调，通知父容器刷新
 */
class TaskListPanel(
    private val storage: TaskStorage,
    private val onTaskChanged: () -> Unit
) : JBBox(BoxLayout.Y_AXIS) {

    /** 当前加载的任务列表 */
    private val tasks = mutableListOf<Task>()
    /** 当前筛选选项 */
    private var currentFilter: FilterOption = FilterOption.ALL

    /** 筛选选项枚举：全部/待办/进行中/完成 */
    private enum class FilterOption(val displayName: String, val status: TaskStatus?) {
        ALL("全部", null),
        TODO("待办", TaskStatus.TODO),
        IN_PROGRESS("进行中", TaskStatus.IN_PROGRESS),
        DONE("完成", TaskStatus.DONE)
    }

    init {
        // 设置四周内边距
        border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        background = JBColor.PanelBackground
        isOpaque = true
    }

    /**
     * 刷新任务列表
     * 从存储重新加载，重新构建整个列表（数据量小，简单可靠）
     * 按创建时间倒序排列，最新任务在最顶部
     */
    fun refresh() {
        removeAll()
        tasks.clear()
        tasks.addAll(storage.loadTasks())

        // 按创建时间倒序排序：最新任务显示在顶部
        tasks.sortByDescending { it.createdAt }

        // 添加顶部筛选和统计栏
        add(createTopBar())

        // 顶部栏和第一个任务之间添加间距（同背景色，不显示分割线，视觉更干净）
        val topSpacing = JPanel()
        topSpacing.background = background
        topSpacing.preferredSize = Dimension(0, 8)
        add(topSpacing)

        // 根据当前筛选条件过滤任务
        val filteredTasks = if (currentFilter.status == null) {
            tasks
        } else {
            tasks.filter { it.status == currentFilter.status }
        }

        // 逐个添加任务卡片，每个任务之间添加 8px 垂直间距
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
                        it.name == existingTask.title // 这只是后备方案，正常不会走到这里
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
            // 任务卡片之间添加垂直间距，同背景色不显示分割线
            val spacing = JPanel()
            spacing.background = background
            spacing.preferredSize = Dimension(0, 8)
            add(spacing)
        }

        // 添加垂直弹性空间，填满剩余空白区域，让任务列表都挤在顶部
        add(JBBox.createVerticalGlue())

        revalidate()
        repaint()
    }

    /**
     * 创建顶部栏：左侧筛选下拉框 + 右侧统计信息
     *
     * 布局技巧：
     * 1. 外层使用 `GridLayout(1, 2)` → 强制左右两个格子宽度完全相等，高度相同
     * 2. 每个格子内部使用 `GridBagLayout` → 设置 `anchor = BASELINE` 保证文字垂直基线对齐
     * 3. `weightx = 1.0` 让内容在格子内水平居中
     * 这样就能确保左侧"筛选"文字和右侧统计文字**永远在同一水平线上完美对齐**
     */
    private fun createTopBar(): JPanel {
        // 外层面板：分成 1 行 2 列，水平间距 10px
        // GridLayout 特性：强制所有格子宽度、高度完全相等
        val panel = JPanel(java.awt.GridLayout(1, 2, 10, 0))
        panel.border = BorderFactory.createEmptyBorder(1, 8, 1, 8)  // 减小上下内边距让整体更紧凑
        panel.background = JBColor.PanelBackground

        // ========== 左边格子：状态筛选下拉框 ==========
        val leftCell = JPanel(GridBagLayout())
        leftCell.background = JBColor.PanelBackground

        val gcLeft = GridBagConstraints()
        // BASELINE 锚点：天然就是水平居中 + 垂直基线对齐，正好满足需求
        gcLeft.anchor = GridBagConstraints.BASELINE
        gcLeft.weightx = 1.0  // 水平方向占满整个格子，保证内容水平居中
        gcLeft.weighty = 0.0  // 垂直方向只需要自身高度，不扩展

        val leftContent = JPanel(FlowLayout(FlowLayout.CENTER, 5, 0))
        leftContent.background = JBColor.PanelBackground
        leftContent.add(JLabel("筛选: "))
        val filterOptions = FilterOption.entries.toTypedArray()
        val comboBox = JComboBox(filterOptions.map { it.displayName }.toTypedArray())
        comboBox.selectedIndex = FilterOption.entries.indexOf(currentFilter)
        comboBox.addActionListener {
            val selectedIndex = comboBox.selectedIndex
            currentFilter = FilterOption.entries[selectedIndex]
            refresh()  // 筛选变化后立即刷新任务列表
        }
        leftContent.add(comboBox)

        leftCell.add(leftContent, gcLeft)
        panel.add(leftCell)

        // ========== 右边格子：任务统计信息 ==========
        val rightCell = JPanel(GridBagLayout())
        rightCell.background = JBColor.PanelBackground

        val gcRight = GridBagConstraints()
        // 同样：水平居中 + 垂直基线对齐，保证和左边对齐
        gcRight.anchor = GridBagConstraints.BASELINE
        gcRight.weightx = 1.0  // 水平方向占满格子，保证水平居中
        gcRight.weighty = 0.0  // 垂直方向只需要自身高度，不扩展

        // 统计各状态的任务数量
        val todoCount = tasks.count { it.status == TaskStatus.TODO }
        val inProgressCount = tasks.count { it.status == TaskStatus.IN_PROGRESS }
        val doneCount = tasks.count { it.status == TaskStatus.DONE }

        // 使用 HTML 实现染色效果：不同状态不同颜色，统计数字加粗
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

    /**
     * 创建水平分隔线（当前未使用，保留以备后用）
     */
    private fun createSeparator(): JPanel {
        val separator = JPanel()
        separator.background = JBColor.GRAY
        separator.preferredSize = Dimension(0, 1)
        separator.alignmentX = CENTER_ALIGNMENT
        return separator
    }
}
