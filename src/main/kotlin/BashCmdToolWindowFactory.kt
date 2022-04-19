import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import utils.BashCmdUtil
import javax.swing.Icon

class BashCmdToolWindowFactory : ToolWindowFactory, DumbAware {

    private var icon: Icon? = null
    private var name: String? = null

    override fun init(toolWindow: ToolWindow) {
        if (icon != null && name != null) {
            toolWindow.setIcon(icon!!)
            toolWindow.stripeTitle = name!!
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        ContentFactory.SERVICE.getInstance()?.let {
            it.createContent(BashCmdToolWindow(project), null, false).run {
                toolWindow.contentManager.addContent(this)
            }
        }
    }

    override fun shouldBeAvailable(project: Project): Boolean {
        val data = BashCmdDataManager.loadToolWindowData(project)
        name = BashCmdUtil.getBashCmdName(data?.name)
        icon = BashCmdUtil.getBashCmdIcon(data?.icon, BashCmdIconType.MAIN, project)
        return BashCmdUtil.isNotEmpty(data?.commands)
    }
}