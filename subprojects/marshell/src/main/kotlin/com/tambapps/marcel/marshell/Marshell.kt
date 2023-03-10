package com.tambapps.marcel.marshell

import com.tambapps.marcel.marshell.console.MarshellCompleter
import com.tambapps.marcel.marshell.console.MarshellSnippetParser
import com.tambapps.marcel.marshell.console.ReaderHighlighter
import com.tambapps.marcel.repl.MarcelShell
import marcel.lang.URLMarcelClassLoader
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException

fun main(args: Array<String>) {
  val marshell = Marshell()
  marshell.run()
}

class Marshell: MarcelShell(URLMarcelClassLoader(Marshell::class.java.classLoader)) {

  private val highlighter = ReaderHighlighter(typeResolver, replCompiler)
  private val reader =  LineReaderBuilder.builder()
    .highlighter(highlighter)
    .parser(MarshellSnippetParser()) // useful for completer. To know from where start completion
    .completer(MarshellCompleter(replCompiler, typeResolver))
    .build()

  override fun readLine(prompt: String): String {
    return reader.readLine(prompt)
  }

  override fun doRun() {
    try {
      super.doRun()
    } catch (e: UserInterruptException) { exit() }
    catch (ee: EndOfFileException) { exit() }
    catch (ex: Exception) { ex.printStackTrace() }
  }

}