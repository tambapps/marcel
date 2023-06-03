package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.DumbbellException
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.exception.MarcelParserException
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ImportNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.repl.command.*
import com.tambapps.marcel.repl.jar.JarWriterFactory
import com.tambapps.marcel.repl.printer.SuspendPrinter
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.util.MarcelVersion
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

abstract class MarcelShell constructor(
  compilerConfiguration: CompilerConfiguration,
  protected val printer: SuspendPrinter,
  val marcelClassLoader: MarcelClassLoader,
  jarWriterFactory: JarWriterFactory,
  protected val tempDir: File,
  val binding: Binding = Binding(),
  protected val typeResolver: ReplJavaTypeResolver = ReplJavaTypeResolver(marcelClassLoader, binding),
  private val promptTemplate: String = "marshell:%03d> ") {

  val lastNode: ClassNode? get() = replCompiler.parserResult?.scriptNode
  val definedTypes get() = typeResolver.definedTypes.filter { !it.isScript }
  val definedFunctions get() = replCompiler.definedFunctions
  val imports: Collection<ImportNode> get() = replCompiler.imports
  abstract val initScriptFile: File?

  protected val replCompiler = MarcelReplCompiler(compilerConfiguration, marcelClassLoader, typeResolver)
  private val evaluator = MarcelEvaluator(binding, replCompiler, marcelClassLoader, jarWriterFactory, tempDir)
  private val buffer = mutableListOf<String>()
  private val commands = mutableListOf<ShellCommand>(
          ClearBufferCommand(),
          ExitCommand(),
          HelpCommand(),
          ImportCommand(),
          ListCommand(),
          PullDependencyCommand(),
  )
  private val runningReference = AtomicBoolean()

  abstract suspend fun readLine(prompt: String): String

  suspend fun run() {
    runningReference.set(true)
    val initScriptFile = this.initScriptFile
    if (initScriptFile != null && initScriptFile.isFile) {
      val text = initScriptFile.readText()
      if (text.isNotBlank()) {
        try {
          evaluator.eval(text)
        } catch (e: MarcelLexerException) {
          println("Error from init script: ${e.message}")
          onInitScriptFail(e)
        } catch (e: MarcelSemanticException) {
          println("Error from init script: ${e.message}")
          onInitScriptFail(e)
        } catch (e: MarcelParserException) {
          println("Error from init script: ${e.message}")
          onInitScriptFail(e)
        } catch (ex: Exception) {
          println("Error from init script: ${ex.message}")
          onInitScriptFail(ex)
        }
      }
    }
    onStart()
    while (runningReference.get()) {
      doRun()
    }
    onFinish()
  }

  suspend fun evalJarFile(jarFile: File, className: String?) {
    try {
      val eval = evaluator.evalJarFile(jarFile, className)
      printEval(eval)
    } catch (ex: Exception) {
      printer.suspendPrintln("${ex.javaClass.name}: ${ex.message}")
    }
  }

  open suspend fun doRun() {
    val prompt = String.format(promptTemplate, buffer.size)
    val line = readLine(prompt)
    if (line.isEmpty()) return
    if (isCommand(line)) {
      val args = line.split("\\s+".toRegex())
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
        onPreEval(text)
        val eval = evaluator.eval(text)
        onPostEval(text, eval)
        buffer.clear()
        printEval(eval)
      } catch (e: MarcelLexerException) {
        printer.suspendPrintln("Error: ${e.message}")
        buffer.clear()
      } catch (e: MarcelSemanticException) {
        printer.suspendPrintln(e.message)
        buffer.clear()
      } catch (e: DumbbellException) {
        printer.suspendPrintln("Error while pulling a dumbbell: ${e.message}")
        buffer.clear()
      } catch (e: MarcelParserException) {
        if (e.isEof) {
          buffer.add(line)
        } else {
          printer.suspendPrintln(e.message)
          buffer.clear()
        }
      } catch (ex: Exception) {
        printer.suspendPrintln("${ex.javaClass.name}: ${ex.message}")
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

  protected open suspend fun onFinish() {
  }


  protected open fun onInitScriptFail(e: Exception) {
  }

  protected open suspend fun printEval(eval: Any?) {
    printer.suspendPrint(eval)
  }

  protected open suspend fun onPreEval(text: String) {}
  protected open suspend fun onPostEval(text: String, eval: Any?) {}


  fun addCommand(c: ShellCommand) {
    this.commands.add(c)
  }

  fun addImport(importArgs: String) {
    replCompiler.addImport("import $importArgs")
  }

}