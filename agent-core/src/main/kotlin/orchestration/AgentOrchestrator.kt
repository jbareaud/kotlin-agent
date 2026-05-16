package org.jbareaud.agent.core.orchestration

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.ToolExecutionResultMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.ChatRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jbareaud.agent.core.chat.ChatDelegate
import org.jbareaud.agent.core.chat.StreamingChatModelEvent
import org.jbareaud.agent.core.commands.CommandRegistry
import org.jbareaud.agent.tools.ToolRegistry
import org.jbareaud.agent.tools.toolables.cwd
import org.jbareaud.agent.tui.TuiWrapper
import java.time.LocalDateTime

class AgentOrchestrator(
    private val chatDelegate: ChatDelegate,
    private val toolRegistry: ToolRegistry,
    private val commandRegistry: CommandRegistry,
    private val tui: TuiWrapper,
) {
    private val messages = mutableListOf<ChatMessage>().apply {
        add(buildSystemPrompt())
    }

    private val lock = Mutex()

    private fun buildSystemPrompt(): ChatMessage {
        val systemPrompt = """
                You are an expert coding assistant. You help users with coding tasks by reading files, executing commands, editing code, and writing new files.
                
                Available tools:
                - read: Read file contents
                - bash: Execute bash commands (ls, grep, find, etc.)
                - edit: Make surgical edits to files (find exact text and replace)
                - write: Create or overwrite files

                Guidelines:
                - Always use bash tool for file operations like ls, grep, find
                - Use read to examine files before editing
                - Use edit for precise changes (old text must match exactly)
                - Use write only for new files or complete rewrites
                - Be concise in your responses
                - Show file paths clearly when working with files
                
                Current date and time: ${LocalDateTime.now()}
                Current working directory: ${cwd()}
        """.trimIndent()
        return SystemMessage.from(systemPrompt)
    }

    suspend fun start() = coroutineScope {
        tui.title()
        while (true) {
            val input = tui.input() ?: continue
            when {
                input.isBlank() -> continue
                commandRegistry.isCommand(input) -> {
                    commandRegistry.executeCommand(input, this)
                }
                else -> {
                    messages.add(UserMessage.from(input))
                    process(input)
                }
            }
        }
    }

    private suspend fun process(input: String) = coroutineScope {
        lock.withLock {
            tui.label("Agent:")
            do {
                var loop = false
                val chatRequest = buildChatRequest()
                chatDelegate.generateAsFlow(chatRequest)
                    .onEach { event ->
                        when(event) {
                            is StreamingChatModelEvent.CompleteResponse -> {
                                messages.add(event.response.aiMessage())
                                tui.complete()
                            }
                            is StreamingChatModelEvent.CompleteToolCall -> {
                                tui.info("(Calling tool ${event.toolExecutionRequest.name()})")
                                handleToolCall(event.toolExecutionRequest)
                                loop = true
                            }
                            is StreamingChatModelEvent.Error -> {
                                tui.error(event.cause.toString())
                            }
                            is StreamingChatModelEvent.PartialResponse -> {
                                tui.partial(event.partialResponse)
                            }
                            is StreamingChatModelEvent.PartialThinking -> {
                                tui.info("(Thinking)")
                            }
                            is StreamingChatModelEvent.PartialToolCall -> {
                                tui.info("(Calling tool ${event.name})")
                            }
                        }
                    }
                    .onCompletion { /* noop */ }
                    .catch { tui.error(it.toString()) }
                    .collect { /* noop */ }
            } while (loop)
        }
    }

    private fun buildChatRequest(): ChatRequest {
        return ChatRequest.Builder()
            .messages(messages)
            //.parameters()
            //.responseFormat()
            .toolSpecifications(toolRegistry.listTools())
            .build()
    }

    private suspend fun handleToolCall(toolCallRequest: ToolExecutionRequest) {
        val result = toolRegistry.execute(toolCallRequest)
        if (result.isError) {
            tui.error("(Tool call in error : ${result.result()})")
        }
        messages.add(
            ToolExecutionResultMessage.Builder()
                .id(toolCallRequest.id())
                .toolName(toolCallRequest.name())
                .attributes(result.attributes())
                .isError(result.isError)
                .contents(result.resultContents())
                .build()
        )
    }
}