package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.data.ShellSession
import com.tambapps.marcel.android.marshell.room.entity.CacheableScript
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors

class AndroidMarshellRunner(
  val session: ShellSession,
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

  suspend fun evalCachedScript(script: CacheableScript) = withContext(Dispatchers.IO) {
    val jarFile = File(session.directory, "${script.name}.dex")
    jarFile.writeBytes(script.cachedJar!!)
    shell.evalJarFile(jarFile, script.scriptClassName, script.dumbbells!!)
  }

  fun stop() {
    runBlocking { shell.dispose() }
    executor.shutdown()
  }
}