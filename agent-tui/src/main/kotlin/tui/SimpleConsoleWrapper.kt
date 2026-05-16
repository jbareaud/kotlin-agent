package org.jbareaud.agent.tui


class SimpleConsoleWrapper /* : TuiWrapper */ {
    fun title() {
        // noop
    }

    fun printResponse(partialResponse: String?) {
        print(partialResponse)
    }

    fun complete() {
        println("${System.lineSeparator()}(RESPONSE COMPLETE)")
    }

    fun printError(errorResponse: String?) {
        System.err.println("ERROR : $errorResponse")
    }

    fun input(): String? {
        return readlnOrNull()
    }

    fun info(str: String) {
        // noop
    }
}