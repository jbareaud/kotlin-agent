package org.jbareaud.agent.core

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jbareaud.agent.core.chat.ChatDelegate
import org.jbareaud.agent.core.commands.CommandRegistry
import org.jbareaud.agent.core.orchestration.AgentOrchestrator
import org.jbareaud.agent.tools.ToolRegistry
import org.jbareaud.agent.tui.BasicMordantWrapper

fun main(args: Array<String>): kotlin.Unit = runBlocking {
    val tui = BasicMordantWrapper()

    val toolRegistry = ToolRegistry()
    val chatDelegate = ChatDelegate()
    val commandRegistry = CommandRegistry()

    val orchestrator = AgentOrchestrator(chatDelegate, toolRegistry, commandRegistry, tui)

    launch {
        orchestrator.start()
    }
}

