package com.tambapps.marcel.android.marshell.repl

import android.os.Build
import android.util.Log
import com.tambapps.marcel.android.marshell.data.Prompt
import com.tambapps.marcel.android.marshell.repl.console.TextViewHighlighter
import com.tambapps.marcel.android.marshell.repl.jar.DexJarWriterFactory
import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.printer.SuspendPrinter
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.util.MarcelVersion
import java.io.File

class AndroidMarshell constructor(
  compilerConfiguration: CompilerConfiguration,
  classesDir: File,
  override val initScriptFile: File?,
  out: SuspendPrinter,
  marcelClassLoader: MarcelClassLoader,
  binding: Binding,
  private val readLineFunction: suspend (String) -> String,
  private val history: MutableList<Prompt>
) : MarcelShell(compilerConfiguration, out, marcelClassLoader,
  DexJarWriterFactory(),
  classesDir, binding, PROMPT_TEMPLATE) {

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
    Log.e("cacaca", history.toString())
  }
}