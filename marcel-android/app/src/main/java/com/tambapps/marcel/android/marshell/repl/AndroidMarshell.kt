package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.android.marshell.data.Prompt
import com.tambapps.marcel.android.marshell.repl.console.TextViewHighlighter
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.ReplJavaTypeResolver
import com.tambapps.marcel.repl.printer.SuspendPrinter
import kotlinx.coroutines.runBlocking
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import java.io.File

class AndroidMarshell constructor(
  compilerConfiguration: CompilerConfiguration,
  classesDir: File,
  override val initScriptFile: File?,
  out: SuspendPrinter,
  val exitFunc: () -> Unit,
  marcelClassLoader: MarcelClassLoader,
  binding: Binding,
  typeResolver: ReplJavaTypeResolver,
  private val readLineFunction: suspend (String) -> String,
  private val history: MutableList<Prompt>
) : MarcelShell(
  compilerConfiguration = compilerConfiguration,
  printer = out,
  marcelClassLoader = marcelClassLoader,
  jarWriterFactory = DexJarWriterFactory(),
  tempDir = classesDir,
  binding = binding,
  typeResolver = typeResolver,
  promptTemplate = PROMPT_TEMPLATE) {

  companion object {
    const val PROMPT_TEMPLATE = "%03d> "
  }

  override suspend fun readLine(prompt: String): String {
    return readLineFunction.invoke(prompt)
  }

  override suspend fun printVersion() {
    // version is printed manually anyway so this method shouldn't be called
  }

  fun newHighlighter(): TextViewHighlighter {
    return TextViewHighlighter(typeResolver, replCompiler)
  }

  override suspend fun printEval(eval: Any?) {
    printer.suspendPrintln(eval)
  }

  override suspend fun onPostEval(text: String, eval: Any?) {
    history.add(Prompt(text, eval))
  }

  override suspend fun exit() {
    super.exit()
    exitFunc.invoke()
  }
  fun dispose() {
    runBlocking { super.exit() }
  }
}