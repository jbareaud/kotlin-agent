package org.jbareaud.agent.tools

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.agent.tool.ToolSpecifications
import dev.langchain4j.data.message.Content
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.service.tool.DefaultToolExecutor
import dev.langchain4j.service.tool.ToolExecutionResult

class ToolRegistry {
    companion object {
        const val ID = "id"
        const val NAME = "name"
    }

    private val specs =
        load().associate { classWithTools ->
            val newSpecs = ToolSpecifications.toolSpecificationsFrom(classWithTools)
            newSpecs.first().name() to ToolDef(newSpecs.first(), classWithTools)
        }

    fun listTools(): List<ToolSpecification> = specs.values.map { it.spec }

    fun execute(toolRequest: ToolExecutionRequest): ToolExecutionResult {
        val toolDef = specs[toolRequest.name()]
        if (toolDef != null) {
            return try {
                val classWithTool = toolDef.classWithTools.getDeclaredConstructor().newInstance()
                val executor = DefaultToolExecutor(classWithTool, toolRequest)
                val result = executor.execute(toolRequest, null)
                toolRequest.toToolExecutionResult { listOf(TextContent.from(result)) }
            } catch(err: ToolException) {
                toolRequest.toToolExecutionResult(true) {
                    mutableListOf(TextContent.from(err.message)).apply {
                        if (err.cause != null) {
                            add(TextContent.from(err.cause.toString()))
                        }
                    } as List<Content>
                }
            }
        }
        return toolRequest.toToolExecutionResult(true) {
            listOf(TextContent.from("Tool ${toolRequest.name()} is unknown"))
        }
    }

    private fun ToolExecutionRequest.toToolExecutionResult(
        isError: Boolean = false,
        resultProducer: () -> List<Content>,
    ): ToolExecutionResult {
        return ToolExecutionResult.builder()
            .resultContents(resultProducer())
            .attributes(
                mapOf(
                    ID to id(),
                    NAME to name(),
                )
            )
            .isError(isError)
            .build()

    }
}
