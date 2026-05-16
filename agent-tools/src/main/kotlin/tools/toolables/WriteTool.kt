package org.jbareaud.agent.tools.toolables

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.jbareaud.agent.tools.Toolable
import kotlin.io.path.writeText

@Toolable
class WriteTool {
    @Tool(
        name = "writeTool",
        value = ["""
Write content to a file. Creates the file if it doesn't exist, overwrites if it does. Automatically creates parent directories.
Use write only for new files or complete rewrites.
        """],
    )
    fun writeTool(
        @P("Path to the file to write (relative or absolute)")
        path: String,
        @P("Content to write to the file")
        content: String,
    ) {
        val resolvedPath = resolvePath(path)
        // TODO check / create dir if needed
        resolvedPath.writeText(content)
    }
}