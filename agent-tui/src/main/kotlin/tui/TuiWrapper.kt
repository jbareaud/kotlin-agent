package org.jbareaud.agent.tui

interface TuiWrapper {
    suspend fun title()
    suspend fun label(label: String)
    suspend fun partial(partial: String?)
    suspend fun complete()
    suspend fun error(error: String?)
    suspend fun input(): String?
    suspend fun info(str: String)
}