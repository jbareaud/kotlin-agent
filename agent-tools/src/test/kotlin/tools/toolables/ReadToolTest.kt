package tools.toolables

import org.jbareaud.agent.tools.ToolException
import org.jbareaud.agent.tools.toolables.ReadTool
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ReadToolTest {

    private val tool = ReadTool()

    @Test
    fun `Should start from line 1 if offset not set`() {
        val filename = "numeroted_lines.txt"
        val filepath = requireNotNull(this::class.java.classLoader.getResource(filename)?.path)
        val results = tool.readTool(filepath, limit = 10)
        assertEquals(10, results.size)
        assertEquals(
            listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
            results.map { it.toInt() },
        )
    }

    @Test
    fun `Should read the first 10 lines`() {
        val filename = "numeroted_lines.txt"
        val filepath = requireNotNull(this::class.java.classLoader.getResource(filename)?.path)
        val results = tool.readTool(filepath, 0, 10)
        assertEquals(10, results.size)
        assertEquals(
            listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10),
            results.map { it.toInt() },
        )
    }

    @Test
    fun `Should read until EOF when limit is null`() {
        val filename = "numeroted_lines.txt"
        val filepath = requireNotNull(this::class.java.classLoader.getResource(filename)?.path)
        val results = tool.readTool(filepath, 0)
        assertEquals(100, results.size)
        assertEquals(
            (1..100).toList(),
            results.map { it.toInt() },
        )
    }

    @Test
    fun `Should raise exception if offset is bigger than file lines number`() {
        val filename = "numeroted_lines.txt"
        val filepath = requireNotNull(this::class.java.classLoader.getResource(filename)?.path)
        assertThrows(ToolException::class.java) {
            tool.readTool(filepath, 101)
        }
    }
}