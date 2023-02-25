package com.tambapps.marcel.marshell

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.marshell.command.ExitCommand
import com.tambapps.marcel.marshell.command.HelpCommand
import com.tambapps.marcel.marshell.command.ListCommand
import com.tambapps.marcel.marshell.command.ShellCommand
import com.tambapps.marcel.marshell.console.MarshellCompleter
import com.tambapps.marcel.marshell.console.MarshellSnippetParser
import com.tambapps.marcel.marshell.console.ReaderHighlighter
import com.tambapps.marcel.marshell.repl.MarcelEvaluator
import com.tambapps.marcel.marshell.repl.MarcelReplCompiler
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.MarcelField
import marcel.lang.Binding
import marcel.lang.util.MarcelVersion
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import java.nio.file.Files

class Shell {

  val binding = Binding()
  val lastNode: ClassNode? get() = replCompiler.parserResult?.scriptNode
  private val typeResolver = JavaTypeResolver()
  private val tempDir = Files.createTempDirectory("marshell")
  private val replCompiler = MarcelReplCompiler(CompilerConfiguration.DEFAULT_CONFIGURATION, typeResolver)
  private val evaluator = MarcelEvaluator(binding, replCompiler, tempDir.toFile())
  private val highlighter = ReaderHighlighter(typeResolver, replCompiler)
  private val reader =  LineReaderBuilder.builder()
    .highlighter(highlighter)
    .parser(MarshellSnippetParser()) // useful for completer. To know from where start completion
    .completer(MarshellCompleter(replCompiler, typeResolver))
    .build()

  private val buffer = mutableListOf<String>()
  private val commands = listOf<ShellCommand>(
    HelpCommand(),
    ExitCommand(),
    ListCommand()
  )

  init {
    typeResolver.loadDefaultExtensions()
  }


  fun run() {
    println("Marshell (Marcel: ${MarcelVersion.VERSION}, Java: " + System.getProperty("java.version") + ")")
    while (true) {
      try {
        val prompt = String.format("marshell:%03d> ", buffer.size)
        val line = reader.readLine(prompt)
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
          } catch (e: MarcelSemanticException) {e.printStackTrace()
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
      } catch (e: UserInterruptException) { break }
      catch (ee: EndOfFileException) { break }
      catch (ex: Exception) { ex.printStackTrace() }
    }
    Files.delete(tempDir)
  }

  fun printHelp() {
    commands.forEach { it.printHelp(System.out) }
  }

  fun findCommand(name: String): ShellCommand? {
    return commands.find { it.name == name || it.shortName == name }
  }

  fun exit() {
    // TODO
  }

  private fun isCommand(line: String): Boolean {
    return line.startsWith(":")
  }
}