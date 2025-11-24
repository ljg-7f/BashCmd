package actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.ui.AnimatedIcon
import kotlinx.coroutines.*
import utils.BashCmdUtil
import utils.getBashCmdToolWindow

class BashCmdToolWindowRefreshAction : DumbAwareAction() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isRefreshing = false

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }

    override fun actionPerformed(e: AnActionEvent) {
        if (isRefreshing) return
        isRefreshing = true
        e.presentation.icon = AnimatedIcon.Default() // 刷新动效icon
        e.project?.getBashCmdToolWindow()?.run { refresh() } // 刷新tool window

        // 延迟以展示刷新动效
        scope.launch {
            delay(500)
            e.presentation.icon = BashCmdUtil.getBashCmdIcon("AllIcons.Actions.Refresh")
            isRefreshing = false
        }
    }
}