package com.tambapps.marcel.marshell

import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.marshell.console.MarshellCompleter
import com.tambapps.marcel.marshell.console.MarshellSnippetParser
import com.tambapps.marcel.marshell.console.ReaderHighlighter
import com.tambapps.marcel.parser.exception.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.repl.MarcelShell
import com.tambapps.marcel.repl.jar.BasicJarWriterFactory
import com.tambapps.marcel.repl.printer.PrintStreamSuspendPrinter
import marcel.lang.URLMarcelClassLoader
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import java.io.File
import kotlin.system.exitProcess

suspend fun main(args: Array<String>) {
  val marshell = Marshell()
  marshell.run()
}

class Marshell: MarcelShell(
  PrintStreamSuspendPrinter(System.out), URLMarcelClassLoader(Marshell::class.java.classLoader), BasicJarWriterFactory(),
  "marshell:%03d> ") {

  private val highlighter = ReaderHighlighter(typeResolver, replCompiler)
  private val reader = LineReaderBuilder.builder()
    .highlighter(highlighter)
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
    val marcelHome = File(
      System.getenv("MARCEL_HOME")
        ?: (System.getenv("HOME") + "/.marcel/")
    )
    val initScriptFile = File(marcelHome, "marshell/init.mcl")
    if (initScriptFile.exists()) {
      val text = initScriptFile.readText()
      if (text.isNotBlank()) {
        try {
          evaluator.eval(text)
        } catch (e: MarcelLexerException) {
          println("Error from init script: ${e.message}")
          exitProcess(1)
        } catch (e: MarcelSemanticException) {
          println("Error from init script: ${e.message}")
          exitProcess(1)
        } catch (e: MarcelParserException) {
          println("Error from init script: ${e.message}")
          exitProcess(1)
        } catch (ex: Exception) {
          println("Error from init script: ${ex.message}")
          exitProcess(1)
        }
      }
    }
  }

}