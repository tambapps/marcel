package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.dumbbell.Dumbbell
import com.tambapps.marcel.dumbbell.DumbbellException
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.repl.command.*
import com.tambapps.marcel.repl.jar.JarWriterFactory
import com.tambapps.marcel.repl.printer.Printer
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.util.MarcelVersion
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.StringWriter
import java.util.concurrent.atomic.AtomicBoolean

abstract class MarcelShell constructor(
  compilerConfiguration: CompilerConfiguration,
  protected val printer: Printer,
  val marcelClassLoader: MarcelClassLoader,
  jarWriterFactory: JarWriterFactory,
  protected val tempDir: File,
  val binding: Binding = Binding(),
  protected val symbolResolver: ReplMarcelSymbolResolver = ReplMarcelSymbolResolver(marcelClassLoader, binding),
  private val promptTemplate: String = "marshell:%03d> ") {

  val lastNode: ClassNode? get() = replCompiler.semanticResult?.scriptNode
  val definedTypes get() = symbolResolver.definedTypes.filter { !it.isScript }
  val definedFunctions get() = replCompiler.definedFunctions
  val imports: Collection<ImportNode> get() = replCompiler.imports
  abstract val initScriptFile: File?

  protected val replCompiler = MarcelReplCompiler(compilerConfiguration, marcelClassLoader, symbolResolver)
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
    onFinish()
  }

  // used by android marshell
  suspend fun evalJarFile(jarFile: File, className: String?, dumbbells: List<String>) {
    try {
      // import dumbbells
      for (artifactString in dumbbells) {
        val pulledArtifacts = Dumbbell.pull(artifactString)
        pulledArtifacts.forEach {
          if (it.jarFile != null) {
            marcelClassLoader.addLibraryJar(it.jarFile)
          }
        }
      }

      // then run script
      val eval = evaluator.evalJarFile(jarFile, className)
      printEval(eval)
    } catch (e: Exception) {
      printer.println("${e.javaClass.name}: ${e.message}")
    } catch (e: DumbbellException) {
      printer.println("Error while pulling a dumbbell: ${e.message}")
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
        printer.println("Unknown command $commandName")
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
    printer.print("Marshell (Marcel: ${MarcelVersion.VERSION}, Java: " + System.getProperty("java.version") + ")")
  }

  protected open suspend fun onStart() {
  }

  protected open suspend fun onFinish() {
  }


  protected open fun onInitScriptFail(e: Exception) {
  }

  protected open suspend fun printEval(eval: Any?) {
    printer.print(eval)
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