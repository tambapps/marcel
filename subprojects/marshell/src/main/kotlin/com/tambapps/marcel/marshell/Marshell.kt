package com.tambapps.marcel.marshell

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.marshell.console.MarshellCompleter
import com.tambapps.marcel.marshell.console.MarshellSnippetParser
import com.tambapps.marcel.marshell.console.ReaderHighlighter
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.jar.BasicJarWriterFactory
import com.tambapps.marcel.repl.printer.PrintStreamSuspendPrinter
import marcel.lang.Binding
import marcel.lang.URLMarcelClassLoader
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import java.io.File
import java.nio.file.Files
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {
  val marshell = Marshell()
  marshell.run()
}

class Marshell: MarcelShell(
        compilerConfiguration = CompilerConfiguration(dumbbellEnabled = true),
        printer = PrintStreamSuspendPrinter(System.out),
        marcelClassLoader = URLMarcelClassLoader(Marshell::class.java.classLoader),
        jarWriterFactory = BasicJarWriterFactory(),
        tempDir = Files.createTempDirectory("marshell").toFile(),
        binding = Binding(),
        promptTemplate = "marshell:%03d> ") {

  private val marcelHome get() = File(System.getenv("MARCEL_HOME") ?: (System.getenv("HOME") + "/.marcel/"))

  override val initScriptFile: File get() = File(marcelHome, "marshell/init.mcl")

  private val highlighter = ReaderHighlighter(typeResolver, replCompiler)
  private val reader = LineReaderBuilder.builder()
    .highlighter(highlighter)
    // to store history into a file
    .variable(LineReader.HISTORY_FILE, File(marcelHome, "marshell/marshell.history"))
    .parser(MarshellSnippetParser()) // useful for completer. To know from where start completion
    .completer(MarshellCompleter(replCompiler, typeResolver))
    .build()

  override suspend fun readLine(prompt: String): String {
    println()
    return reader.readLine(prompt)
  }

  override suspend fun doRun() {
    try {
      super.doRun()
    } catch (e: UserInterruptException) { exit() }
    catch (ee: EndOfFileException) { exit() }
    catch (ex: Exception) { ex.printStackTrace() }
  }


  override suspend fun onStart() {
    printVersion()
  }

  override suspend fun onFinish() {
    tempDir.deleteRecursively()
  }

  override fun onInitScriptFail(e: Exception) {
    exitProcess(1)
  }
}