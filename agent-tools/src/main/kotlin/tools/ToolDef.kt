package org.jbareaud.agent.tools

import dev.langchain4j.agent.tool.ToolSpecification

data class ToolDef(val spec: ToolSpecification, val classWithTools: Class<*>)
