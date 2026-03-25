package com.example.tasks.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

/**
 * 任务工具窗口工厂
 * 实现 IntelliJ Platform 的 [ToolWindowFactory] 接口，负责创建并注册任务管理工具窗口到 IDE 侧边栏
 */
class TasksToolWindowFactory : ToolWindowFactory {
    /**
     * 创建工具窗口内容
     * @param project 当前项目
     * @param toolWindow 工具窗口实例
     */
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = TasksToolWindowContent(project)
        val contentFactory = toolWindow.contentManager.factory
        val toolWindowContent = contentFactory.createContent(content, "", false)
        toolWindow.contentManager.addContent(toolWindowContent)
    }

    /**
     * 判断工具窗口是否应该对当前项目可用
     * @param project 当前项目
     * @return 永远返回 true，表示对所有项目都可用
     */
    override fun shouldBeAvailable(project: Project): Boolean {
        return true
    }
}
