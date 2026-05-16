package org.jbareaud.agent.tools.toolables

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.jbareaud.agent.tools.ToolException
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.notExists

//@Toolable - disabled
class LsTool {
    companion object {
        const val DEFAULT_LIMIT = 500
    }

    @Tool(
        name = "lsTool",
        value = ["""
List directory contents. Returns entries sorted alphabetically, with '/' suffix for directories. Includes dotfiles. 
Output is truncated to {limit} entries.
"""],
    )
    fun lsTool(
        @P("Directory to list (default: current directory)") path: String = Paths.get("").toAbsolutePath().toString(),
        @P("Maximum number of entries to return (default: 500)") limit: Int = DEFAULT_LIMIT,
    ): List<String> {
        val dirPath = Path(path)
        if (dirPath.notExists()) {
            throw ToolException("Path not found : $path")
        }
        if (dirPath.isDirectory().not()) {
            throw ToolException("Not a directory : $path")
        }
        val entries = try {
            dirPath.listDirectoryEntries()
        } catch (t: Throwable) {
            throw ToolException("Cannot read directory : $path", t)
        }
        val sortedEntries = entries.sortedBy { it.name.lowercase() }

        val results = mutableListOf<String>()
        var limitReached = false
        for (entry in sortedEntries) {
            if (results.size >= limit) {
                limitReached = true
                break
            }
            val fullPath = entry.toAbsolutePath().absolutePathString()
            val suffix = if (entry.isDirectory()) "/" else ""
            results.add("$fullPath$suffix")
        }
        if (limitReached) {
            results.add("($limit entries limit reached. Use limit=${limit * 2} for more)")
        }
        return results
    }
}