package console

import actions.BashCmdConsoleRunAction
import actions.BashCmdConsoleStopAction
import com.intellij.execution.Executor
import com.intellij.execution.configurations.PtyCommandLine
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ColoredProcessHandler
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.ide.CommonActionsManager
import com.intellij.notification.NotificationDisplayType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Separator
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import runners.AbstractConsoleRunner
import utils.BashCmdUtil
import utils.BashInterpreterDetection

class BashCmdConsoleRunner(
    project: Project,
    consoleTitle: String,
    workingDir: String?,
    private val commands: String,
    private val finishTip: String?
) : AbstractConsoleRunner<ConsoleView>(project, consoleTitle, workingDir) {

    private var process: Process? = null

    override fun createConsoleView(): ConsoleView {
        return TextConsoleBuilderFactory.getInstance().createBuilder(project).console
    }

    override fun createProcess(): Process {
        // 解决执行 python 脚本时不实时输出
        return PtyCommandLine().withWorkDirectory(workingDir).apply {
            exePath = BashInterpreterDetection.findBestLocation()
            addParameters("-c")
            addParameters(commands)
        }.createProcess().also { process = it }
    }

    override fun createProcessHandler(process: Process): OSProcessHandler {
        return ColoredProcessHandler(process, null).apply {
            finishTip?.let {
                addProcessListener(object : ProcessAdapter() {
                    override fun processTerminated(event: ProcessEvent) {
                        if (event.exitCode == 0) {
                            BashCmdUtil.log(it, NotificationDisplayType.BALLOON)
                        }
                    }
                })
            }
        }
    }

    override fun fillToolBarActions(
        toolbarActions: DefaultActionGroup, defaultExecutor: Executor?, contentDescriptor: RunContentDescriptor?
    ): List<AnAction> {
        val actionList: MutableList<AnAction> = ArrayList()

        //run
        actionList.add(BashCmdConsoleRunAction(project, process, consoleTitle, commands, finishTip))

        //stop
        actionList.add(BashCmdConsoleStopAction(process, processHandler, consoleTitle))

        //分隔符
        actionList.add(Separator.create())

        //close
        actionList.add(createCloseAction(defaultExecutor, contentDescriptor))

        //help
        actionList.add(CommonActionsManager.getInstance().createHelpAction("interactive_console"))

        toolbarActions.addAll(actionList)
        return actionList
    }

    override fun getConsoleIcon() = BashCmdUtil.getBashCmdIcon("AllIcons.Actions.Execute")
    override fun isAutoFocusContent() = false

    companion object {
        fun start(project: Project?, consoleTitle: String, commands: String, finishTip: String?) {
            try {
                FileDocumentManager.getInstance().saveAllDocuments() // 保存文件
                project?.let {
                    BashCmdConsoleRunner(
                        it,
                        consoleTitle,
                        it.basePath,
                        handleCommands(it, commands),
                        finishTip
                    ).initAndRun()
                }
            } catch (e: Exception) {
                BashCmdUtil.log(" execute bash command [$commands] is error: $e")
            }
        }

        private fun handleCommands(project: Project, commands: String): String {
            // 将 commands 中的 #projectPath# 替换为根目录
            return addShellEnvPath(BashCmdUtil.replacePathMark(commands, project)!!)
        }

        // bugfix: Android Studio Bumblebee | 2021.1.1 版本找不到 kkr 和 adb 的环境路径
        private fun addShellEnvPath(commands: String): String {
            return "source ~/.bash_profile > /dev/null 2>&1 ; source ~/.zsh_profile > /dev/null 2>&1 ; $commands"
        }
    }
}