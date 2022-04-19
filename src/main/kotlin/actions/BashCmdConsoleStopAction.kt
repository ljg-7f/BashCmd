package actions

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import utils.BashCmdUtil

class BashCmdConsoleStopAction(
    private val process: Process?,
    private val handle: ProcessHandler,
    name: String
) : DumbAwareAction("Stop: $name", "Stop: $name", BashCmdUtil.getBashCmdIcon("AllIcons.Actions.Suspend")) {

    override fun update(e: AnActionEvent) {
        process?.let { e.presentation.isEnabled = it.isAlive }
    }

    override fun actionPerformed(e: AnActionEvent) {
        handle.destroyProcess()
    }
}