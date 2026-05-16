package tools.toolables

import org.jbareaud.agent.tools.ToolException
import org.jbareaud.agent.tools.toolables.BashTool
import org.jbareaud.agent.tools.toolables.CommandResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BashToolTest {

    private val tool = BashTool()

    @Test
    fun `ls command should succeed`() {
        val result = tool.bashTool("ls -la .")
        assertEquals(CommandResult.CommandResultType.SUCCEEDED, result.executionResult)
        assertTrue(result.output.isNotBlank())
    }

    @Test
    fun `Dummy command should fail`() {
        assertThrows<ToolException> {
            tool.bashTool("laaaaaa foo -la .")
        }
    }

    @Test
    fun `pipe command should succeed`() {
        val result = tool.bashTool("ls -la -R . | grep BashToolTest.kt")
        assertEquals(CommandResult.CommandResultType.SUCCEEDED, result.executionResult)
        assertTrue(result.output.isNotBlank())
    }
}