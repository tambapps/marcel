package com.tambapps.marcel.android.marshell.data

import android.content.Context
import android.util.Log
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import java.util.Collections
import java.util.LinkedList
import java.util.UUID

/**
 * Class holding all objects holding a shell's data
 */
class ShellSession private constructor(
  val name: String, val binding: Binding, val classLoader: MarcelDexClassLoader,
  val typeResolver: ReplJavaTypeResolver, val history: MutableList<Prompt>, val directory: File) {

  companion object {
    // TODO say no to static
    var COUNT = 0

    fun newSession(context: Context): ShellSession {
      val binding = Binding()
      val classLoader = MarcelDexClassLoader()
      return ShellSession(
        name = if (++COUNT == 1) "marshell" else "marshell ($COUNT)",
        binding = binding,
        classLoader = classLoader,
        typeResolver = ReplJavaTypeResolver(classLoader, binding),
        history = Collections.synchronizedList(LinkedList()),
        // TODO clean sessions dir at some point
        directory = File(context.getDir("shell_sessions", Context.MODE_PRIVATE), "session" +
            this.hashCode().toString())
      )
    }
  }

  init {
    if (!directory.mkdir()) {
      Log.e("ShellSession", "Couldn't create shell session directory")
    }
  }


  fun dispose() {
    directory.deleteRecursively()
  }
}

data class Prompt(val input: String, val output: Any?)