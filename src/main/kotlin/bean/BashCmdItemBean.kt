package bean

class BashCmdItemBean {
    var cmd: String? = null
    var name: String? = null
    var icon: String? = null
    var desc: String? = null
    var shortcut: String? = null
    var finishTip: String? = null
    var isSeparator: Boolean = false
    var isPopupMode: Boolean = false
    var children: ArrayList<BashCmdItemBean>? = null

    override fun toString() = name ?: ""
}