package com.tambapps.marcel.android.marshell.data

import android.content.Context
import android.util.Log
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import java.util.Collections
import java.util.LinkedList

// TODO variables are not well handled when they are defined
/**
 * Class holding all objects holding a shell's data
 */
class ShellSession constructor(context: Context) {
  val binding = Binding()
  val classLoader = MarcelDexClassLoader()
  val typeResolver = ReplJavaTypeResolver(classLoader, binding)
  // synchronized because entries are put from a non main-threads
  val history: MutableList<Prompt> = Collections.synchronizedList(LinkedList())
  val directory = File(context.getDir("shell_sessions", Context.MODE_PRIVATE), "session" +
      this.hashCode().toString())

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