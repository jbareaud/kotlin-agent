package tools.toolables

import org.jbareaud.agent.tools.toolables.LsTool
import org.junit.jupiter.api.Test

class LsToolTest {

    private val tool = LsTool()

    @Test
    fun test() {
        val entries = tool.lsTool()

    }
}