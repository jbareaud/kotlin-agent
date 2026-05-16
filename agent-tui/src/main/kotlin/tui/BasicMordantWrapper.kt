package org.jbareaud.agent.tui

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.prompt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BasicMordantWrapper: TuiWrapper {

    val terminal = Terminal()

    override suspend fun title() {
        decorate {
            terminal.println(
                """
 █████   ████           █████    ████   ███                  █████████                                 █████   
░░███   ███░           ░░███    ░░███  ░░░                  ███░░░░░███                               ░░███    
 ░███  ███     ██████  ███████   ░███  ████  ████████      ░███    ░███   ███████  ██████  ████████   ███████  
 ░███████     ███░░███░░░███░    ░███ ░░███ ░░███░░███     ░███████████  ███░░███ ███░░███░░███░░███ ░░░███░   
 ░███░░███   ░███ ░███  ░███     ░███  ░███  ░███ ░███     ░███░░░░░███ ░███ ░███░███████  ░███ ░███   ░███    
 ░███ ░░███  ░███ ░███  ░███ ███ ░███  ░███  ░███ ░███     ░███    ░███ ░███ ░███░███░░░   ░███ ░███   ░███ ███
 █████ ░░████░░██████   ░░█████  █████ █████ ████ █████    █████   █████░░███████░░██████  ████ █████  ░░█████ 
░░░░░   ░░░░  ░░░░░░     ░░░░░  ░░░░░ ░░░░░ ░░░░ ░░░░░    ░░░░░   ░░░░░  ░░░░░███ ░░░░░░  ░░░░ ░░░░░    ░░░░░  
                                                                         ███ ░███                              
                                                                        ░░██████                               
                                                                         ░░░░░░                                
""".trim('\n')
            )
        }
    }

    override suspend fun label(label: String) {
        decorate {
            terminal.println(TextColors.white(label))
        }
    }

    override suspend fun partial(partial: String?) {
        decorate {
            if (partial != null) {
                terminal.print(TextColors.green(partial).trim())
            }
        }
    }

    override suspend fun complete() {
        decorate {
            terminal.println(TextColors.blue("(COMPLETE)"))
        }
    }

    override suspend fun error(error: String?) {
        decorate {
            terminal.println(TextColors.brightRed("(Error: $error)"))
        }
    }

    override suspend fun input(): String? {
        return decorate {
            terminal.prompt("User")
        }
    }

    override suspend fun info(str: String) {
        decorate {
            terminal.println(TextColors.gray(str))
        }
    }

    private suspend fun <T> decorate(block: () -> T?): T? {
        return withContext(Dispatchers.IO) {
            block()
        }
    }
}
