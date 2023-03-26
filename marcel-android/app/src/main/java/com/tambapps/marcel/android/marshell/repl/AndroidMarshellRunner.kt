package com.tambapps.marcel.android.marshell.repl

import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executors

class AndroidMarshellRunner(
  val shell: AndroidMarshell
) {
  private val executor = Executors.newSingleThreadExecutor()

  fun start() {
    executor.submit {
      runBlocking {
        shell.run()
      }
    }
  }

  fun stop() {
    runBlocking { shell.exit() }
    executor.shutdown()
  }
}