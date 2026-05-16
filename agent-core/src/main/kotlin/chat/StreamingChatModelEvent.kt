package org.jbareaud.agent.core.chat

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.model.chat.response.ChatResponse

sealed interface StreamingChatModelEvent {
    data class PartialResponse(
        val partialResponse: String
    ) : StreamingChatModelEvent

    data class CompleteResponse(
        val response: ChatResponse
    ) : StreamingChatModelEvent

    data class Error(
        val cause: Throwable
    ) : StreamingChatModelEvent

    data class PartialToolCall(
        val name: String,
        val partialArguments: String? = null,
    ) : StreamingChatModelEvent

    data class CompleteToolCall(
        var toolExecutionRequest: ToolExecutionRequest
    ) : StreamingChatModelEvent

    data class PartialThinking(
        val partialThinking: String
    ) : StreamingChatModelEvent
}