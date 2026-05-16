package org.jbareaud.agent.core.chat

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.*
import dev.langchain4j.model.ollama.OllamaStreamingChatModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ChatDelegate {

    private var currentChatModel: StreamingChatModel =
        OllamaStreamingChatModel.OllamaStreamingChatModelBuilder()
            .baseUrl("http://localhost:11434")
            .temperature(0.6)
            //.logRequests(true)
            //.logResponses(true)
            .modelName("qwen3.5:9b")
            .think(true)
            .build()

    fun switch(newStreamingChatModel: StreamingChatModel) {
        TODO("not implemented yet")
    }

    fun generateAsFlow(chatRequest: ChatRequest): Flow<StreamingChatModelEvent> {
        return callbackFlow {
            val handler =
                object : StreamingChatResponseHandler {
                    override fun onPartialResponse(token: String) {
                        trySend(StreamingChatModelEvent.PartialResponse(token))
                    }

                    override fun onCompleteResponse(completeResponse: ChatResponse) {
                        trySend(StreamingChatModelEvent.CompleteResponse(completeResponse))
                        close()
                    }

                    override fun onError(error: Throwable) {
                        trySend(StreamingChatModelEvent.Error(error))
                        close(error)
                    }

                    override fun onPartialToolCall(partialToolCall: PartialToolCall) {
                        trySend(StreamingChatModelEvent.PartialToolCall(partialToolCall.name(), partialToolCall.partialArguments()))
                    }

                    override fun onCompleteToolCall(completeToolCall: CompleteToolCall) {
                        trySend(StreamingChatModelEvent.CompleteToolCall(completeToolCall.toolExecutionRequest()))
                        close()
                    }

                    override fun onPartialThinking(partialThinking: PartialThinking) {
                        trySend(StreamingChatModelEvent.PartialThinking(partialThinking.text()))
                    }
                }

            currentChatModel.chat(chatRequest, handler)

            awaitClose {
                // cleanup
            }
        }
    }
}

