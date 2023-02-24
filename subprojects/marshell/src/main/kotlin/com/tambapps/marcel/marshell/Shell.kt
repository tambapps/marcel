package com.tambapps.marcel.marshell

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.marshell.console.ReaderHighlighter
import com.tambapps.marcel.marshell.repl.MarcelEvaluator
import com.tambapps.marcel.marshell.repl.MarcelReplCompiler
import com.tambapps.marcel.parser.MarcelParserException
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import java.nio.file.Files

class Shell {

  private val typeResolver = JavaTypeResolver()
  private val tempDir = Files.createTempDirectory("marshell")
  private val replCompiler = MarcelReplCompiler(CompilerConfiguration.DEFAULT_CONFIGURATION, typeResolver)
  private val evaluator = MarcelEvaluator(replCompiler, tempDir.toFile())
  private val highlighter = ReaderHighlighter(typeResolver, replCompiler)
  private val reader: LineReader
  private val buffer = mutableListOf<String>()

  init {
    typeResolver.loadDefaultExtensions()
    val readerBuilder = LineReaderBuilder.builder()
      .highlighter(highlighter)

    reader = readerBuilder.build()
  }


  fun run() {
    while (true) {
      try {
        val prompt = if (buffer.isEmpty()) "> " else "  "
        val line = reader.readLine(prompt)
        //highlighter.highlight(reader, line) // this is for debug through intelij
        if (line.startsWith(":")) {
          // handle command
          println("Unknown command " + line.substring(1))
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

        // TODO
      } catch (e: UserInterruptException) { break }
      catch (ee: EndOfFileException) { break }
      catch (ex: Exception) { ex.printStackTrace() }
    }
    Files.delete(tempDir)
  }

  private fun isCommand(line: String): Boolean {
    return line.startsWith(":")
  }
}