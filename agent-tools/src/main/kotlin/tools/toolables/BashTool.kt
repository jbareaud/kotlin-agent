package org.jbareaud.agent.tools.toolables

import dev.langchain4j.agent.tool.P
import dev.langchain4j.agent.tool.Tool
import org.jbareaud.agent.tools.ToolException
import org.jbareaud.agent.tools.Toolable
import java.util.concurrent.TimeUnit

@Toolable
class BashTool {
    companion object {
        internal data class DangerousPattern(val regex: Regex, val desc: String)

        private val patterns = listOf<DangerousPattern>(
            DangerousPattern("\\brm\\s+(-[^\\s]*r|--recursive)".toRegex(), "recursive delete"),  // rm -rf, rm -r, rm --recursive
            DangerousPattern("\\bsudo\\b".toRegex(), "sudo command"),  // sudo anything
            DangerousPattern("\\b(chmod|chown)\\b.*777".toRegex(), "dangerous permissions"),  // chmod 777, chown 777
            DangerousPattern("\\bmkfs\\b".toRegex(), "filesystem format"),  // mkfs.ext4, mkfs.xfs
            DangerousPattern("\\bdd\\b.*\\bof=/dev/".toRegex(), "raw device write"),  // dd if=x of=/dev/sda
            DangerousPattern(">\\s*/dev/sd[a-z]".toRegex(), "raw device overwrite"),  // echo x > /dev/sda
            DangerousPattern("\\bkill\\s+-9\\s+-1\\b".toRegex(), "kill all processes"),  // kill -9 -1
            DangerousPattern(":\\(\\)\\s*\\{\\s*:\\s*\\|\\s*:\\s*&\\s*\\}\\s*;".toRegex(), "fork bomb") // :(){:|:&};
        )
    }

    @Tool(
        name = "bashTool",
        value = ["""
Execute a bash command ((ls, grep, find, etc.)) in the current working directory. Returns stdout and stderr. 
Optionally provide a timeout in seconds.            
"""],
    )
    fun bashTool(
        @P("Bash command to execute")
        command: String,
        @P("Timeout in seconds (optional, no default timeout)")
        timeout: Long? = null,
    ): CommandResult {
        val processBuilder = ProcessBuilder()
        val environment = processBuilder.environment()
        environment["HOME"] = System.getenv("HOME")

        val builders =
            command.split("|")
                .map { subcommand ->
                    ProcessBuilder(
                        *subcommand
                            .check()
                            .split(" ")
                            .filter { !it.isBlank() }
                            .toTypedArray()
                    )
                }

        val processes =
            try {
                ProcessBuilder.startPipeline(builders)
            } catch (err: Exception) {
                throw ToolException(message = "Impossible to run command : $command", cause = err)
            }

        val last = processes[processes.size - 1]

        val exitCode =
            try {
                if (timeout != null) {
                    last.waitFor(timeout, TimeUnit.SECONDS)
                } else {
                    last.waitFor()
                }
            } catch (err: Exception) {
                throw ToolException(message = "Execution failed for command : $command", cause = err)
            }

        return if (exitCode == 0) {
            val iss = last.inputStream.bufferedReader().readText()
            CommandResult(CommandResult.CommandResultType.SUCCEEDED, iss)
        } else {
            for (p in processes.reversed()) {
                val err = p.errorStream.bufferedReader().readText()
                if (!err.isBlank()) {
                    CommandResult(CommandResult.CommandResultType.FAILED, err)
                }
            }
            CommandResult(CommandResult.CommandResultType.FAILED, "(no output)")
        }
    }

    private fun String.check(): String {
        for (pattern in patterns) {
            if (pattern.regex.containsMatchIn(this)) {
                throw ToolException(message = "Unauthorized (${pattern.desc}) command : $this")
            }
        }
        return this
    }
}

data class CommandResult(val executionResult: CommandResultType, val output: String) {
    enum class CommandResultType { SUCCEEDED, FAILED }
}