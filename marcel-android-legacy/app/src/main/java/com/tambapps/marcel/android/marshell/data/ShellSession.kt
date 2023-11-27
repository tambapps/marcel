package com.tambapps.marcel.android.marshell.data

import com.tambapps.marcel.repl.ReplJavaTypeResolver
import marcel.lang.Binding
import marcel.lang.MarcelDexClassLoader
import java.io.File
import java.util.Collections
import java.util.LinkedList

/**
 * Class holding all objects holding a shell's data
 */
class ShellSession private constructor(
  val name: String, val binding: Binding, val classLoader: MarcelDexClassLoader,
  val typeResolver: ReplJavaTypeResolver, val history: MutableList<Prompt>, val directory: File) {

  companion object {
    fun newSession(name: String, directory: File): ShellSession {
      val binding = Binding()
      val classLoader = MarcelDexClassLoader()
      return ShellSession(
        name = name,
        binding = binding,
        classLoader = classLoader,
        typeResolver = ReplJavaTypeResolver(classLoader, binding),
        history = Collections.synchronizedList(LinkedList()),
        directory = directory
      )
    }
  }

  fun dispose() {
    directory.deleteRecursively()
  }
}

data class Prompt(val input: String, val output: Any?)