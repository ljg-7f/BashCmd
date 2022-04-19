package utils

import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.SystemInfoRt
import org.apache.commons.lang.StringUtils
import java.io.File

object BashInterpreterDetection {
    private val POSSIBLE_EXE_LOCATIONS = mutableListOf(
        "/sbin/bash",
        "/bin/bash",
        "/usr/bin/bash",
        "/usr/local/bin/bash",
        "/opt/local/bin/bash",
        "/opt/bin/bash",
        "/sbin/sh",
        "/bin/sh",
        "/usr/bin/sh",
        "/opt/local/bin/sh",
        "/opt/bin/sh",
        "/usr/bin/env"
    )

    private val POSSIBLE_EXE_LOCATIONS_WINDOWS = mutableListOf(
        "c:\\cygwin\\bin\\bash.exe",
        "d:\\cygwin\\bin\\bash.exe"
    )

    fun findBestLocation(): String {
        val locations = if (SystemInfo.isWindows) POSSIBLE_EXE_LOCATIONS_WINDOWS else POSSIBLE_EXE_LOCATIONS
        for (guessLocation in locations) {
            if (isSuitable(guessLocation)) {
                return guessLocation
            }
        }
        val pathLocation = OSUtil.findBestExecutable(if (SystemInfoRt.isWindows) "bash.exe" else "bash") ?: return ""
        return if (isSuitable(pathLocation)) pathLocation else ""
    }

    private fun isSuitable(guessLocation: String): Boolean {
        return File(guessLocation).run { isFile && canRead() && canExecute() }
    }
}

private object OSUtil {
    fun findBestExecutable(commandName: String): String? {
        val pathElements = StringUtils.split(System.getenv("PATH"), File.pathSeparatorChar) ?: return null
        return findBestExecutable(commandName, listOf(*pathElements))
    }

    private fun findBestExecutable(commandName: String, paths: List<String>): String? {
        for (path in paths) {
            findExecutable(commandName, path)?.let { return it }
        }
        return null
    }

    private fun findExecutable(commandName: String, path: String): String? {
        return File(path + File.separatorChar + commandName).run {
            if (exists()) absolutePath else null
        }
    }
}