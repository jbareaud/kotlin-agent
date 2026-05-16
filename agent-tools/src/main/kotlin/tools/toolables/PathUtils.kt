package org.jbareaud.agent.tools.toolables

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path

fun resolvePath(pathStr: String): Path {
    val path = Path(pathStr)
    return if (path.isAbsolute) {
        path
    } else {
        Path(cwd().toString(), pathStr)
    }
}

fun cwd(): Path = Paths.get("").toAbsolutePath()