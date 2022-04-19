package bean

class BashCmdRootBean {
    var name: String? = null
    var icon: String? = null
    var commands: ArrayList<BashCmdItemBean>? = null

    fun isEmpty() = commands == null || commands!!.isEmpty()
}