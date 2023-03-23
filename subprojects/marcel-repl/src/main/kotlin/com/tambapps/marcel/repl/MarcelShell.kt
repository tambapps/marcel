package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.exception.MarcelParserException
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.repl.command.ClearBufferCommand
import com.tambapps.marcel.repl.command.ExitCommand
import com.tambapps.marcel.repl.command.HelpCommand
import com.tambapps.marcel.repl.command.ImportCommand
import com.tambapps.marcel.repl.command.ListCommand
import com.tambapps.marcel.repl.command.PullDependencyCommand
import com.tambapps.marcel.repl.command.ShellCommand
import com.tambapps.marcel.repl.jar.JarWriterFactory
import com.tambapps.marcel.repl.printer.SuspendPrinter
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.util.MarcelVersion
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

abstract class MarcelShell constructor(
  protected val printer: SuspendPrinter,
  val marcelClassLoader: MarcelClassLoader,
  jarWriterFactory: JarWriterFactory,
  protected val tempDir: File,
  private val promptTemplate: String = "marshell:%03d> ") {

  val binding = Binding()
  val lastNode: ClassNode? get() = replCompiler.parserResult?.scriptNode
  val definedClasses get() = replCompiler.definedClasses
  val imports: Collection<ImportNode> get() = replCompiler.imports

  protected val typeResolver = JavaTypeResolver(marcelClassLoader)
  protected val replCompiler = MarcelReplCompiler(CompilerConfiguration(dumbbellEnabled = true), typeResolver)
  protected val evaluator = MarcelEvaluator(binding, replCompiler, marcelClassLoader, jarWriterFactory, tempDir)
  private val buffer = mutableListOf<String>()
  private val commands = listOf<ShellCommand>(
    HelpCommand(),
    ExitCommand(),
    ListCommand(),
    ClearBufferCommand(),
    PullDependencyCommand(),
    ImportCommand(),
  )
  private val runningReference = AtomicBoolean()

  init {
    typeResolver.loadDefaultExtensions()
  }

  fun addImport(importArgs: String) {
    replCompiler.addImport("import $importArgs")
  }

  abstract suspend fun readLine(prompt: String): String

  suspend fun run() {
    runningReference.set(true)
    onStart()
    while (runningReference.get()) {
      doRun()
    }
    onFinish()
  }

  open suspend fun doRun() {
    val prompt = String.format(promptTemplate, buffer.size)
    val line = readLine(prompt)
    if (line.isEmpty()) return
    if (isCommand(line)) {
      val args = line.split(" ")
      val commandName = args[0].substring(1)

      val command = findCommand(commandName)
      if (command != null) {
        command.run(this, args.subList(1, args.size), printer)
      } else {
        printer.suspendPrintln("Unknown command $commandName")
      }
    } else {
      try {
        val text = buffer.joinToString(separator = System.lineSeparator(), postfix = if (buffer.isEmpty()) line else "\n$line")
        val eval = evaluator.eval(text)
        buffer.clear()
        printEval(eval)
      } catch (e: MarcelLexerException) {
        printer.suspendPrintln("Error: ${e.message}")
        buffer.clear()
      } catch (e: MarcelSemanticException) {
        printer.suspendPrintln(e.message)
        buffer.clear()
      } catch (e: MarcelParserException) {
        if (e.eof) {
          buffer.add(line)
        } else {
          printer.suspendPrintln(e.message)
          buffer.clear()
        }
      } catch (ex: Exception) {
        ex.printStackTrace()
        buffer.clear()
      }
    }
  }

  suspend fun printHelp() {
    commands.forEach { it.printHelp(printer) }
  }

  fun findCommand(name: String): ShellCommand? {
    return commands.find { it.name == name || it.shortName == name }
  }

  open suspend fun exit() {
    onExit()
    runningReference.set(false)
  }

  private fun isCommand(line: String): Boolean {
    return line.startsWith(":")
  }

  suspend fun listImports() {
    findCommand("list")!!.run(this, listOf("imports"), printer)

  }
  fun clearBuffer() {
    buffer.clear()
  }


  open suspend fun printVersion() {
    printer.suspendPrint("Marshell (Marcel: ${MarcelVersion.VERSION}, Java: " + System.getProperty("java.version") + ")")
  }

  protected open suspend fun onStart() {
  }

  protected open suspend fun onExit() {
  }

  protected open suspend fun onFinish() {

  }
  protected open suspend fun printEval(eval: Any?) {
    printer.suspendPrint(eval)
  }
}