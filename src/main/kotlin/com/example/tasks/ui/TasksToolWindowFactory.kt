package com.example.tasks.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class TasksToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = TasksToolWindowContent(project)
        val contentFactory = toolWindow.contentManager.factory
        val toolWindowContent = contentFactory.createContent(content, "", false)
        toolWindow.contentManager.addContent(toolWindowContent)
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        return true
    }
}
