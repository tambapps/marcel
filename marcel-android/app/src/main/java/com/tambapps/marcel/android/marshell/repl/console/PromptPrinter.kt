package com.tambapps.marcel.android.marshell.repl.console

import com.tambapps.marcel.android.marshell.ui.screen.shell.Prompt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class PromptPrinter(private val prompts: MutableList<Prompt>): Printer {

    private var newLine = AtomicBoolean(true)
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun print(o: Any?) {
        if (!newLine.get() && prompts.lastOrNull()?.type == Prompt.Type.STDOUT) {
            scope.launch {
                prompts[prompts.lastIndex] = prompt(prompts.last().text.toString() + o)
            }
        } else {
            scope.launch {
                prompts.add(prompt(o))
            }
        }
        newLine.set(false)
    }

    override fun println(o: Any?) {
        scope.launch {
            prompts.add(prompt(o))
        }
        newLine.set(true)
    }

    override fun println() {
        println("")
    }

    private fun prompt(any: Any?) = Prompt(Prompt.Type.STDOUT, any)

}