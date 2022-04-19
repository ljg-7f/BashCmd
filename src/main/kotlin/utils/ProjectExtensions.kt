package utils

import BashCmdToolWindow
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

// 获取 BashCmdToolWindow 实例
fun Project.getBashCmdToolWindow(): BashCmdToolWindow? {
    return ToolWindowManager
        .getInstance(this)
        .getToolWindow("BashCmd.ToolWindow")
        ?.contentManager
        ?.component
        ?.components
        ?.get(0)
        ?.let {
            if (it is BashCmdToolWindow)
                it
            else
                null
        }
}