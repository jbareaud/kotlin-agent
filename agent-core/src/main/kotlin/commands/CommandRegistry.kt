package org.jbareaud.agent.core.commands

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

class CommandRegistry {

    private val commands: List<Command> =
        mutableListOf(
            Command(
                predicate = { input -> input.startsWith("/exit") },
                executor = { _, scope -> scope.cancel() },
            )
            // etc
        )

    fun isCommand(text: String) = commands.any { it.predicate.invoke(text) }

    fun executeCommand(
        text: String,
        scope: CoroutineScope,
    ) {
        commands.first { it.predicate.invoke(text) }
            .executor
            .invoke(text, scope)
    }
}

internal data class Command(
    val predicate: (String) -> Boolean,
    val executor: (String, CoroutineScope) -> Unit,
)