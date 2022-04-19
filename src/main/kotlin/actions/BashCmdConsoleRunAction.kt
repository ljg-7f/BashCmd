package actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import console.BashCmdConsoleRunner
import utils.BashCmdUtil

class BashCmdConsoleRunAction(
    private val project: Project,
    private val process: Process?,
    private val name: String,
    private val command: String,
    private val finishTip: String?
) : DumbAwareAction("Run: $name", "Run: $name", BashCmdUtil.getBashCmdIcon("AllIcons.Actions.Execute")) {

    override fun update(e: AnActionEvent) {
        process?.let { e.presentation.isEnabled = !it.isAlive }
    }

    override fun actionPerformed(e: AnActionEvent) {
        BashCmdConsoleRunner.start(project, name, command, finishTip)
    }
}