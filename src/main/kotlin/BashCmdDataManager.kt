import bean.BashCmdRootBean
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.intellij.openapi.project.Project
import utils.BashCmdUtil
import java.io.File.separator
import java.io.FileReader

object BashCmdDataManager {
    private const val BASH_CMD_MAIN_DIR = "bash_cmd_plugin"
    private const val TOOLBAR_FILE_NAME = "bash_cmd_toolbar.json"
    private const val TOOL_WINDOW_FILE_NAME = "bash_cmd_tool_window.json"

    private val toolBarDataMap: HashMap<Project, BashCmdRootBean?> = HashMap()
    private val toolWindowDataMap: HashMap<Project, BashCmdRootBean?> = HashMap()

    private fun getBashCmdPath(basePath: String, dirName: String, fileName: String): String {
        return basePath + separator + BASH_CMD_MAIN_DIR + separator + dirName + separator + fileName
    }

    private fun loadData(project: Project, fileName: String): BashCmdRootBean? {
        return project.basePath?.let {
            merge(
                getDataFromJson(getBashCmdPath(it, "common", fileName)),
                getDataFromJson(getBashCmdPath(it, "personal", fileName))
            )
        }
    }

    private fun getDataFromJson(path: String): BashCmdRootBean? {
        return try {
            Gson().fromJson<BashCmdRootBean>(JsonReader(FileReader(path)), BashCmdRootBean::class.java)
        } catch (e: Exception) {
            BashCmdUtil.log("get data from [$path] is error: $e", null)
            null
        }
    }

    // 将 common 和 personal 目录中得到的数据对象进行 merge
    private fun merge(common: BashCmdRootBean?, personal: BashCmdRootBean?): BashCmdRootBean? {
        return when {
            BashCmdUtil.isEmpty(common) && BashCmdUtil.isEmpty(personal) -> null
            BashCmdUtil.isNotEmpty(common) && BashCmdUtil.isEmpty(personal) -> common
            BashCmdUtil.isEmpty(common) && BashCmdUtil.isNotEmpty(personal) -> personal
            BashCmdUtil.isNotEmpty(common) && BashCmdUtil.isNotEmpty(personal) -> BashCmdRootBean().apply {
                name = personal!!.name ?: common!!.name
                icon = personal.icon ?: common!!.icon
                commands = common!!.commands!!.also { it.addAll(personal.commands!!) }
            }
            else -> null
        }
    }

    fun reLoadToolWindowData(project: Project): BashCmdRootBean? {
        BashCmdUtil.log("refresh tool window")
        toolWindowDataMap[project] = null
        return loadToolWindowData(project)
    }

    fun reLoadToolbarData(project: Project): BashCmdRootBean? {
        BashCmdUtil.log("refresh toolbar")
        toolBarDataMap[project] = null
        return loadToolbarData(project)
    }

    fun loadToolWindowData(project: Project): BashCmdRootBean? {
        return toolWindowDataMap[project] ?: loadData(
            project,
            TOOL_WINDOW_FILE_NAME
        ).also { toolWindowDataMap[project] = it }
    }

    fun loadToolbarData(project: Project): BashCmdRootBean? {
        return toolBarDataMap[project] ?: loadData(project, TOOLBAR_FILE_NAME).also { toolBarDataMap[project] = it }
    }
}