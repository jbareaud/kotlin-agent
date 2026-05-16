package org.jbareaud.agent.tools.toolables

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.jbareaud.agent.tools.ToolException
import org.jbareaud.agent.tools.Toolable
import kotlin.io.path.isRegularFile
import kotlin.io.path.isSymbolicLink
import kotlin.io.path.notExists
import kotlin.io.path.writeText

@Toolable
class EditTool {
    data class ReplaceEdit(
        @P("Exact text for one targeted replacement. It must be unique in the original file and must not overlap with any other edits[].oldText in the same call.")
        val oldText: String,
        @P("Replacement text for this targeted edit.")
        val newText: String,
    )

    @Tool(
        name = "editTool",
        value = ["""
Use edit for precise changes (edits[].oldText must match exactly).
When changing multiple separate locations in one file, use one edit call with multiple entries in edits[] instead of multiple edit calls.
Each edits[].oldText is matched against the original file, not after earlier edits are applied. Do not emit overlapping or nested edits. Merge nearby changes into one edit.
Keep edits[].oldText as small as possible while still being unique in the file. Do not pad with large unchanged regions.
"""],
    )
    fun editTool(
        @P("Path to the file to edit (relative or absolute)")
        path: String,
        @P("One or more targeted replacements. Each edit is matched against the original file, not incrementally. Do not include overlapping or nested edits. If two changes touch the same block or nearby lines, merge them into one edit instead.")
        edits: List<ReplaceEdit>,
    ): String {
        val resolvedPath = resolvePath(path)
        if (resolvedPath.notExists()) {
            throw ToolException("File not found : $path")
        }
        if (resolvedPath.isSymbolicLink()) {
            throw ToolException("File is a symbolic link : $path")
        }
        if (resolvedPath.isRegularFile().not()) {
            throw ToolException("Not a regular file : $path")
        }
        var text = resolvedPath.toFile().readText(Charsets.UTF_8)
        for (edit in edits) {
            text = text.replace(edit.oldText, edit.newText)
        }
        resolvedPath.writeText(text)
        return "Successfully replaced ${edits.size} block(s) in ${path}."
    }
}
