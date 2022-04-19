package utils

import BashCmdIconType
import bean.BashCmdRootBean
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.util.ImageLoader
import com.intellij.util.ui.JBImageIcon
import java.io.File
import javax.swing.Icon

object BashCmdUtil {
    private const val BASH_CAM_NAME = "Bash Cmd"
    private const val PROJECT_PATH_MARK = "#projectPath#"
    private const val BASH_CAM_ICON_MAIN = "/META-INF/icons/bash_cmd.svg"
    private const val BASH_CAM_ICON_RUN = "/META-INF/icons/run.svg"
    private const val BASH_CAM_ICON_MORE = "/META-INF/icons/more.svg"
    private val ONLY_LOG = NotificationGroup.logOnlyGroup("Bash Cmd Log")
    private val NOTIFY_BALLOON = NotificationGroup.balloonGroup("Bash Cmd Tip")

    fun getBashCmdIcon(
        iconPath: String?,
        type: BashCmdIconType = BashCmdIconType.MAIN,
        project: Project? = null
    ): Icon {

        fun isSvg(path: String) = path.endsWith(".svg")
        fun isSystemIcon(path: String) = path.startsWith("AllIcons.")
        fun isBashCmdIcon(path: String) = path.startsWith("/META-INF/icons/")

        try {
            replacePathMark(iconPath, project)?.let {
                if (isSystemIcon(it) || isBashCmdIcon(it)) {
                    IconLoader.getIcon(it, javaClass).run {
                        if (iconWidth > 1 && iconHeight > 1) {
                            return this
                        }
                    }
                } else if (isSvg(it)) {
                    File(it).takeIf { file -> file.exists() }?.let { file ->
                        ImageLoader.loadCustomIcon(file)?.run {
                            return JBImageIcon(this)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log("get icon of [$iconPath] is error: $e, \nwill use default icon.")
        }

        // 使用默认icon
        return when (type) {
            BashCmdIconType.MAIN -> IconLoader.getIcon(BASH_CAM_ICON_MAIN, javaClass)
            BashCmdIconType.RUN -> IconLoader.getIcon(BASH_CAM_ICON_RUN, javaClass)
            BashCmdIconType.MORE -> IconLoader.getIcon(BASH_CAM_ICON_MORE, javaClass)
        }
    }

    fun getBashCmdName(name: String?) = name ?: BASH_CAM_NAME

    // 将 #projectPath# 替换为根目录
    fun replacePathMark(originPath: String?, project: Project?): String? {
        project?.basePath?.takeIf { isNotEmpty(originPath) && originPath!!.contains(PROJECT_PATH_MARK) }?.let {
            return originPath!!.replace(PROJECT_PATH_MARK, it)
        }
        return originPath
    }

    /**
     * 输出日志信息
     * type 为  null   时, 只输出日志到 .../idea-sandbox/system/log/idea.log 中.
     * type 为  NONE   时, 同时会将日志输出到 Event Log 中.
     * type 为 BALLOON 时, 会有弹窗提示, 同时也会输出到 Event Log 和 idea.log 中.
     */

    fun log(content: String, type: NotificationDisplayType? = NotificationDisplayType.NONE) {
        when (type) {
            null -> null
            NotificationDisplayType.NONE -> ONLY_LOG
            else -> NOTIFY_BALLOON
        }?.createNotification(BASH_CAM_NAME, null, content, NotificationType.INFORMATION)?.notify(null)

        Logger.getInstance(BASH_CAM_NAME).warn(content)
    }

    fun isEmpty(c: Collection<*>?) = c == null || c.isEmpty()
    fun isNotEmpty(c: Collection<*>?) = !isEmpty(c)
    fun isEmpty(s: String?) = s == null || s.isEmpty()
    fun isNotEmpty(s: String?) = !isEmpty(s)
    fun isEmpty(b: BashCmdRootBean?) = b == null || b.isEmpty()
    fun isNotEmpty(b: BashCmdRootBean?) = !isEmpty(b)
}