import bean.BashCmdItemBean
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.WindowManager
import console.BashCmdConsoleRunner
import utils.BashCmdUtil
import javax.swing.Icon

/**
 * 注: 多个 project 对应一个 BashCmdToolbarActionGroup 对象
 */
class BashCmdToolbarActionGroup : ActionGroup(), DumbAware {

    private var actionMap: HashMap<Project, Array<AnAction>?> = HashMap()

    override fun update(e: AnActionEvent) {
        e.project?.let {
            val data = BashCmdDataManager.loadToolbarData(it)
            val show = BashCmdUtil.isNotEmpty(data?.commands)
            e.presentation.isVisible = show
            if (show) {
                e.presentation.icon = BashCmdUtil.getBashCmdIcon(data?.icon, BashCmdIconType.MAIN, it)
                e.presentation.text = BashCmdUtil.getBashCmdName(data?.name)
                e.presentation.description = RUN_MAIN_DESCRIPTION
            }
        }
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        return e?.project?.let {
            actionMap[it] ?: getActionList(it).apply { actionMap[it] = this }
        } ?: AnAction.EMPTY_ARRAY
    }

    private fun refresh(project: Project) {
        BashCmdDataManager.reLoadToolbarData(project)?.run {
            actionMap[project] = getActionList(project)
        }
    }

    private fun getActionList(project: Project): Array<AnAction> {
        val list = ArrayList<AnAction>()

        // 创建 toolbar 中的命令
        BashCmdDataManager.loadToolbarData(project)?.let {
            it.commands?.forEach { commandItem ->
                createBashCmdAction(project, commandItem, true)?.run {
                    list.add(this)
                }
            }
        }

        // 添加内部命令: 刷新 toolbar
        if (list.isNotEmpty()) {
            list.add(Separator.create())
            list.add(RefreshToolbarAction(project, INNER_ACTION_REFRESH_NAME))
        }

        return list.toTypedArray()
    }

    private fun createBashCmdAction(project: Project, item: BashCmdItemBean, isTopLevel: Boolean): AnAction? {
        return when {
            item.isSeparator -> Separator.create()
            BashCmdUtil.isEmpty(item.children) && BashCmdUtil.isNotEmpty(item.name) -> BashCmdAction(
                project,
                item,
                isTopLevel
            )
            BashCmdUtil.isNotEmpty(item.children) && BashCmdUtil.isNotEmpty(item.name) -> BashCmdActionGroup(
                project,
                item,
                isTopLevel
            )
            else -> null
        }
    }

    private inner class BashCmdAction(
        private val project: Project,
        private val item: BashCmdItemBean,
        isTopLevel: Boolean
    ) : DumbAwareAction(
        item.name, item.desc ?: item.name, getToolbarIcon(isTopLevel, item, project)
    ), ShortcutSet {

        init {
            item.shortcut?.let {
                registerCustomShortcutSet(this, WindowManager.getInstance().getIdeFrame(project)?.component)
            }
        }

        override fun actionPerformed(e: AnActionEvent) {
            item.cmd?.let {
                BashCmdConsoleRunner.start(e.project, item.desc ?: item.name!!, it, item.finishTip)
            }
        }

        override fun getShortcuts(): Array<Shortcut> {
            return if (BashCmdUtil.isNotEmpty(item.shortcut)) {
                CustomShortcutSet.fromString(item.shortcut).shortcuts
            } else {
                Shortcut.EMPTY_ARRAY
            }
        }
    }

    private inner class BashCmdActionGroup(
        private val project: Project,
        private val item: BashCmdItemBean,
        isTopLevel: Boolean
    ) : ActionGroup(
        item.name, item.desc ?: item.name, getToolbarIcon(isTopLevel, item, project)
    ), DumbAware {

        init {
            isPopup = true
        }

        override fun getChildren(e: AnActionEvent?): Array<AnAction> {
            val list = ArrayList<AnAction>()
            item.children?.forEach { commandItem ->
                createBashCmdAction(project, commandItem, false)?.run {
                    list.add(this)
                }
            }
            return list.toTypedArray()
        }
    }

    private inner class RefreshToolbarAction(private val project: Project, name: String) :
        DumbAwareAction(
            name,
            name,
            BashCmdUtil.getBashCmdIcon("/META-INF/icons/refresh.svg", BashCmdIconType.RUN)
        ) {
        override fun actionPerformed(e: AnActionEvent) {
            refresh(project)
        }
    }

    private companion object {
        private const val RUN_MAIN_DESCRIPTION = "Run bash commands"
        private const val INNER_ACTION_REFRESH_NAME = "refresh command list"

        // 为顶层设置默认 icon
        private fun getToolbarIcon(isTopLevel: Boolean, item: BashCmdItemBean, project: Project): Icon? {
            return if (isTopLevel || BashCmdUtil.isNotEmpty(item.icon)) {
                BashCmdUtil.getBashCmdIcon(
                    item.icon,
                    if (BashCmdUtil.isEmpty(item.children))
                        BashCmdIconType.RUN
                    else
                        BashCmdIconType.MORE,
                    project
                )
            } else {
                null
            }
        }
    }
}