package com.tambapps.marcel.android.marshell.repl.console

import com.tambapps.marcel.android.marshell.ui.component.Prompt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class PromptPrinter(private val prompts: MutableList<Prompt>): Printer {

    private var newLine = AtomicBoolean(true)
    private val scope = CoroutineScope(Dispatchers.Main)

    /*
  TODO handle
  java.lang.AssertionError: Failed to close dex file in finalizer.
      at dalvik.system.DexFile.finalize(DexFile.java:388)
      at java.lang.Daemons$FinalizerDaemon.doFinalize(Daemons.java:339)
      at java.lang.Daemons$FinalizerDaemon.processReference(Daemons.java:324)
      at java.lang.Daemons$FinalizerDaemon.runInternal(Daemons.java:300)
      at java.lang.Daemons$Daemon.run(Daemons.java:145)
      at java.lang.Thread.run(Thread.java:1012)
   */
    override fun print(o: Any?) {
        if (!newLine.get() && prompts.lastOrNull()?.type == Prompt.Type.STDOUT) {
            scope.launch {
                prompts[prompts.lastIndex] = prompt(prompts.last().text + o)
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

    private fun prompt(any: Any?) = Prompt(Prompt.Type.STDOUT, any?.toString() ?: "null")

}