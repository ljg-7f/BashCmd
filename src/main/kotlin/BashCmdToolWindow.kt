import bean.BashCmdItemBean
import bean.BashCmdRootBean
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.JBMenuItem
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.JBColor
import com.intellij.ui.SideBorder
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.components.BorderLayoutPanel
import console.BashCmdConsoleRunner
import utils.BashCmdUtil
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer

/**
 * 注: 一个 project 对应一个 BashCmdToolWindow 对象
 */
class BashCmdToolWindow(private val project: Project) : BorderLayoutPanel(), DumbAware {

    var tree: Tree? = null
    val commandJsp = JBScrollPane().apply {
        border = SideBorder(JBColor.border(), SideBorder.NONE)
    }

    init {
        background = JBColor.background() // 解决改变 tool window 大小时背景颜色不一致

        // 添加 tool window 顶部的 toolbar
        val actionManager = ActionManager.getInstance()
        val actionGroup = actionManager.getAction("BashCmd.ToolWindowTopActions") as DefaultActionGroup
        val toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLWINDOW_TITLE, actionGroup, true)
        toolbar.setTargetComponent(this)
        addToTop(toolbar.component)

        // 添加指令集面板
        injectCommandJsp()
        addToLeft(commandJsp)

        // 动态设置 Jsp 的大小, 解决横向滚动条不可用
        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                commandJsp.preferredSize = size
            }
        })
    }

    fun refresh() = BashCmdDataManager.reLoadToolWindowData(project)?.run { injectCommandJsp() }

    private fun injectCommandJsp() = commandJsp.setViewportView(getCommandTree())

    private fun getCommandTree(): Tree {
        val data = BashCmdDataManager.loadToolWindowData(project)
        val rootNode = DefaultMutableTreeNode(BashCmdUtil.getBashCmdName(data?.name).toLowerCase().replace(" ", "-"))
        addChildren(rootNode, data)
        return Tree(rootNode).apply {
            cellRenderer = BashCmdTreeCellRenderer()
            addMouseListener(BashCmdMouseListener(this))
        }.also { tree = it }
    }

    private fun addChildren(rootNode: DefaultMutableTreeNode, data: BashCmdRootBean?) {
        data?.let {
            it.commands?.forEach { itemCommand ->
                traversalNode(itemCommand)?.run { rootNode.add(this) }
            }
        }
    }

    private fun traversalNode(item: BashCmdItemBean): DefaultMutableTreeNode? {
        if (BashCmdUtil.isEmpty(item.name)) {
            return null
        }

        val node = DefaultMutableTreeNode(item)
        item.takeIf { !it.isPopupMode }?.children?.forEach { itemCommand ->
            traversalNode(itemCommand)?.run { node.add(this) }
        }
        return node
    }

    private fun obtainBashCmdItemBean(value: Any?): BashCmdItemBean? {
        return if (value is DefaultMutableTreeNode && value.userObject is BashCmdItemBean) {
            value.userObject as BashCmdItemBean
        } else {
            null
        }
    }

    private inner class BashCmdTreeCellRenderer : DefaultTreeCellRenderer() {

        init {
            // 解决编译器不同 Theme 下 item 的背景问题
            borderSelectionColor = null
            backgroundSelectionColor = null
            backgroundNonSelectionColor = JBColor.background()
        }

        override fun getTreeCellRendererComponent(
            tree: JTree?, value: Any?, sel: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean
        ): Component {
            obtainBashCmdItemBean(value)?.let {
                leafIcon = BashCmdUtil.getBashCmdIcon(
                    it.icon,
                    if (it.isPopupMode) BashCmdIconType.MORE else BashCmdIconType.RUN,
                    project
                )
            }
            return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)
        }
    }

    private inner class BashCmdMouseListener(private val tree: Tree) : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent?) {
            obtainBashCmdItemBean(tree.lastSelectedPathComponent)?.run {
                if (isPopupMode) {
                    e?.takeIf { it.clickCount == 1 }?.let {
                        showPopupMenu(tree, it, children)
                    }
                } else {
                    e?.takeIf { it.clickCount == 2 }?.let {
                        if ((tree.lastSelectedPathComponent as DefaultMutableTreeNode).isLeaf) {
                            exec(this)
                        }
                    }
                }
            }
        }

        private fun exec(item: BashCmdItemBean) {
            if (BashCmdUtil.isNotEmpty(item.cmd) && BashCmdUtil.isNotEmpty(item.name)) {
                BashCmdConsoleRunner.start(project, item.desc ?: item.name!!, item.cmd!!, item.finishTip)
            }
        }

        private fun showPopupMenu(tree: Tree, e: MouseEvent, items: List<BashCmdItemBean>?) {
            if (BashCmdUtil.isEmpty(items) || !clickInRightPosition(e)) {
                return
            }
            JBPopupMenu().apply {
                for ((index, item) in items!!.withIndex()) {
                    add(JBMenuItem(item.name).apply {
                        addActionListener { exec(item) }
                    })
                    if (index != items.size - 1) {
                        addSeparator()
                    }
                }
                isBorderPainted = false
            }.show(tree, e.x, e.y)
        }

        private fun clickInRightPosition(e: MouseEvent): Boolean {
            val rowHeight = tree.rowHeight
            val position = tree.getRowForPath(tree.anchorSelectionPath) * rowHeight
            return e.y >= position && e.y <= position + rowHeight
        }
    }
}
