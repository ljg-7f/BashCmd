import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import utils.BashCmdUtil
import javax.swing.Icon

class BashCmdToolWindowFactory : ToolWindowFactory, DumbAware {

    private var bashIcon: Icon? = null
    private var bashName: String? = null

    override fun init(toolWindow: ToolWindow) {
        bashIcon?.let { toolWindow.setIcon(it) }
        bashName?.let { toolWindow.stripeTitle = it }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        ContentFactory.getInstance()?.let {
            it.createContent(BashCmdToolWindow(project), null, false).run {
                toolWindow.contentManager.addContent(this)
            }
        }
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        val data = BashCmdDataManager.loadToolWindowData(project)
        bashName = BashCmdUtil.getBashCmdName(data?.name)
        bashIcon = BashCmdUtil.getBashCmdIcon(data?.icon, BashCmdIconType.MAIN, project)
        return BashCmdUtil.isNotEmpty(data?.commands)
    }
}