package com.tambapps.marcel.marshell

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.DumbbellException
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.marshell.command.ClearBufferCommand
import com.tambapps.marcel.marshell.command.ExitCommand
import com.tambapps.marcel.marshell.console.MarshellCompleter
import com.tambapps.marcel.marshell.console.MarshellSnippetParser
import com.tambapps.marcel.marshell.console.ReaderHighlighter
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.marshell.command.HelpCommand
import com.tambapps.marcel.marshell.command.ImportCommand
import com.tambapps.marcel.marshell.command.ListCommand
import com.tambapps.marcel.marshell.command.PullDependencyCommand
import com.tambapps.marcel.marshell.command.ShellCommand
import com.tambapps.marcel.repl.MarcelEvaluator
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.repl.jar.BasicJarWriterFactory
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import marcel.lang.Binding
import marcel.lang.URLMarcelClassLoader
import marcel.lang.util.MarcelVersion
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  val marshell = Marshell(System.out)
  marshell.run()
}

fun tempDir(): File {
  val f = Files.createTempDirectory("marshell").toFile()
  f.deleteOnExit()
  return f
}

class Marshell(private val printer: PrintStream) {

  private val binding = Binding()
  private val classLoader = URLMarcelClassLoader(Marshell::class.java.classLoader)
  private val symbolResolver = ReplMarcelSymbolResolver(classLoader)
  private val replCompiler = MarcelReplCompiler(CompilerConfiguration(dumbbellEnabled = true), classLoader, symbolResolver)
  val evaluator = MarcelEvaluator(binding, replCompiler, classLoader, BasicJarWriterFactory(), tempDir())

  private val marcelHome get() = File(System.getenv("MARCEL_HOME") ?: (System.getenv("HOME") + "/.marcel/"))

  private val initScriptFile: File get() = File(marcelHome, "marshell/init.mcl")

  private val highlighter = ReaderHighlighter(replCompiler)
  private val reader = LineReaderBuilder.builder()
    .highlighter(highlighter)
    // to store history into a file
    .variable(LineReader.HISTORY_FILE, File(marcelHome, "marshell/marshell.history"))
    .parser(MarshellSnippetParser()) // useful for completer. To know from where start completion
    .completer(MarshellCompleter(replCompiler, symbolResolver))
    .build()

  private val runningReference = AtomicBoolean()
  private val buffer = mutableListOf<String>()
  private val commands = listOf<ShellCommand>(
    ClearBufferCommand(),
    ExitCommand(),
    HelpCommand(),
    ImportCommand(),
    ListCommand(),
    PullDependencyCommand(),
  )

  fun printHelp() {
    commands.forEach { it.printHelp(printer) }
  }

  fun findCommand(name: String): ShellCommand? {
    return commands.find { it.name == name || it.shortName == name }
  }

  private fun isCommand(line: String): Boolean {
    return line.startsWith(":")
  }

  fun listImports() {
    findCommand("list")!!.run(this, listOf("imports"), printer)
  }

  private fun readLine(linesBufferSize: Int): String? {
    println()
    return try {
      val hint = binding.getVariableOrNull<Any?>("_hint")?.toString() ?: "marshell"
      reader.readLine(String.format("$hint:%03d> ", linesBufferSize))
    } catch (e: UserInterruptException) {
      exit()
      null
    }
    catch (ee: EndOfFileException) {
      exit()
      null
    }
  }

  fun run() {
    runningReference.set(true)
    val initScriptFile = this.initScriptFile
    if (initScriptFile.isFile) {
      val text = initScriptFile.readText()
      if (text.isNotBlank()) {
        try {
          evaluator.eval(text)
        } catch (ex: Exception) {
          printer.println("Error from init script: ${ex.message}")
          if (ex !is MarcelSemanticException && ex !is MarcelParserException && ex !is MarcelLexerException) {
            val baos = ByteArrayOutputStream()
            ex.printStackTrace(PrintStream(baos, true))
            printer.println(baos.toString())
          }
          onInitScriptFail(ex)
        }
      }
    }
    onStart()
    while (runningReference.get()) {
      doRun()
    }
  }

  private fun doRun() {
    val line = readLine(buffer.size)
    if (line.isNullOrEmpty()) return
    if (isCommand(line)) {
      val args = line.split("\\s+".toRegex())
      val commandName = args[0].substring(1)

      val command = findCommand(commandName)
      if (command != null) {
        command.run(this, args.subList(1, args.size), printer)
      } else {
        printer.println("Unknown command $commandName")
      }
    } else {
      try {
        val text = buffer.joinToString(separator = System.lineSeparator(), postfix = if (buffer.isEmpty()) line else "\n$line")
        val eval = evaluator.eval(text)
        buffer.clear()
        printer.print(eval)
      } catch (e: MarcelLexerException) {
        printer.println("Error: ${e.message}")
        buffer.clear()
      } catch (e: MarcelSemanticException) {
        printer.println(e.message)
        buffer.clear()
      } catch (e: DumbbellException) {
        printer.println("Error while pulling a dumbbell: ${e.message}")
        buffer.clear()
      } catch (e: MarcelParserException) {
        if (e.isEof) {
          buffer.add(line)
        } else {
          printer.println(e.message)
          buffer.clear()
        }
      } catch (ex: Exception) {
        printer.println("Error ${ex.javaClass.name}: ${ex.message}")
        buffer.clear()
        ex.printStackTrace()
      }
    }
  }

  fun clearBuffer() {
    buffer.clear()
  }

  private fun onStart() {
    printer.print("Marshell (Marcel: ${MarcelVersion.VERSION}, Java: " + System.getProperty("java.version") + ")")
  }

  private fun onInitScriptFail(e: Exception) {
    exitProcess(1)
  }

  fun addLibraryJar(jarFile: File) {
    classLoader.addJar(jarFile)
  }
  fun exit() {
    runningReference.set(false)
  }
}