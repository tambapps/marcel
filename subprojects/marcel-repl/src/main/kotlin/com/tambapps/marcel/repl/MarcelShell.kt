package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.repl.command.ClearBufferCommand
import com.tambapps.marcel.repl.command.ExitCommand
import com.tambapps.marcel.repl.command.HelpCommand
import com.tambapps.marcel.repl.command.ImportCommand
import com.tambapps.marcel.repl.command.ListCommand
import com.tambapps.marcel.repl.command.PullDependencyCommand
import com.tambapps.marcel.repl.command.ShellCommand
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.util.MarcelVersion
import java.nio.file.Files
import java.util.concurrent.atomic.AtomicBoolean

abstract class MarcelShell constructor(val marcelClassLoader: MarcelClassLoader) {

  val binding = Binding()
  val lastNode: ClassNode? get() = replCompiler.parserResult?.scriptNode
  val definedClasses get() = replCompiler.definedClasses

  protected val typeResolver = JavaTypeResolver(marcelClassLoader)
  private val tempDir = Files.createTempDirectory("marshell")
  protected val replCompiler = MarcelReplCompiler(CompilerConfiguration.DEFAULT_CONFIGURATION, typeResolver)
  private val evaluator = MarcelEvaluator(binding, replCompiler, marcelClassLoader, tempDir.toFile())
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

  abstract fun readLine(prompt: String): String

  fun run() {
    runningReference.set(true)
    println("Marshell (Marcel: ${MarcelVersion.VERSION}, Java: " + System.getProperty("java.version") + ")")
    while (runningReference.get()) {
      doRun()
    }
    Files.delete(tempDir)
  }

  open fun doRun() {
    val prompt = String.format("marshell:%03d> ", buffer.size)
    val line = readLine(prompt)
    if (line.isEmpty()) return
    //highlighter.highlight(reader, line) // this is for debug through intelij
    if (isCommand(line)) {
      val args = line.split(" ")
      val commandName = args[0].substring(1)

      val command = findCommand(commandName)
      if (command != null) {
        command.run(this, args.subList(1, args.size), System.out)
      } else {
        println("Unknown command $commandName")
      }
    } else {
      try {
        val text = buffer.joinToString(separator = "\n", postfix = if (buffer.isEmpty()) line else "\n$line")
        val eval = evaluator.eval(text)
        buffer.clear()
        println(eval)
      } catch (e: MarcelLexerException) {
        println("Error: ${e.message}")
        buffer.clear()
      } catch (e: MarcelSemanticException) {
        println(e.message)
        buffer.clear()
      } catch (e: MarcelParserException) {
        if (e.eof) {
          buffer.add(line)
        } else {
          println(e.message)
          buffer.clear()
        }
      } catch (ex: Exception) {
        ex.printStackTrace()
        buffer.clear()
      }
    }
  }

  fun printHelp() {
    commands.forEach { it.printHelp(System.out) }
  }

  fun findCommand(name: String): ShellCommand? {
    return commands.find { it.name == name || it.shortName == name }
  }

  fun exit() {
    runningReference.set(false)
  }

  private fun isCommand(line: String): Boolean {
    return line.startsWith(":")
  }

  fun clearBuffer() {
    buffer.clear()
  }
}