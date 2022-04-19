package actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.util.ui.tree.TreeUtil
import utils.getBashCmdToolWindow

class BashCmdToolWindowExpandAction : DumbAwareAction() {

    override fun actionPerformed(e: AnActionEvent) {
        e.project?.getBashCmdToolWindow()?.tree?.let {
            if (e.actionManager.getId(this) == "BashCmd.ExpandAll") {
                TreeUtil.expandAll(it)
            } else {
                TreeUtil.collapseAll(it, -1)
            }
        }
    }
}