package com.tambapps.marcel.marshell

import com.tambapps.marcel.compiler.CompilerConfiguration
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.marshell.console.ReaderHighlighter
import com.tambapps.marcel.marshell.repl.MarcelEvaluator
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
  private val evaluator = MarcelEvaluator(CompilerConfiguration.DEFAULT_CONFIGURATION, typeResolver, tempDir.toFile())
  private val reader: LineReader
  private val buffer = mutableListOf<String>()

  init {
    val readerBuilder = LineReaderBuilder.builder()
      .highlighter(ReaderHighlighter(typeResolver, evaluator))

    reader = readerBuilder.build()
  }


  fun run() {
    while (true) {
      try {
        val prompt = if (buffer.isEmpty()) "> " else "  "
        val line = reader.readLine(prompt)
       // ReaderHighlighter(typeResolver, evaluator).highlight(reader, line) this is for debug through intelij
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