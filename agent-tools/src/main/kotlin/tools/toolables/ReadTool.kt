package org.jbareaud.agent.tools.toolables

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.jbareaud.agent.tools.ToolException
import org.jbareaud.agent.tools.Toolable
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.notExists

@Toolable
class ReadTool {
    // TODO images for vision models
    @Tool(
        name = "readTool",
        value = ["""
Read the contents of a text file. Output is truncated to {limit} lines. 
Use offset/limit for large files. When you need the full file, continue with offset until complete.
"""],
    )
    fun readTool(
        @P("Path to the file to read (relative or absolute)")
        path: String,
        @P("Line number to start reading from (0-indexed)")
        offset: Int = 0,
        @P("Maximum number of lines to read")
        limit: Int? = null,
    ): List<String> {
        val filePath = Path(path)
        if (filePath.notExists()) {
            throw ToolException("File not found : $filePath")
        }
        if (filePath.isSymbolicLink()) {
            throw ToolException("File is a symbolic link : $filePath")
        }
        if (filePath.isRegularFile().not()) {
            throw ToolException("Not a regular file : $filePath")
        }
        val lines = filePath.toFile().readLines(Charsets.UTF_8)
        if (offset > lines.size) {
            throw ToolException("Offset $offset is beyond end of file (${lines.size} lines total)")
        }
        val results = mutableListOf<String>()
        val range = if (limit != null) {
            offset..<offset+limit
        } else {
            // if not limit specified, read until end of file
            offset..<lines.size
        }
        for (index in range) {
            if (index >= lines.size) {
                break
            }
            results.add(lines[index])
        }
        return results
    }
}