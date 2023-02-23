package com.tambapps.marcel.marshell

import com.tambapps.marcel.marshell.console.ReaderHighlighter
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException

class Shell {

  val reader: LineReader

  init {
    val readerBuilder = LineReaderBuilder.builder()
      .highlighter(ReaderHighlighter())

    reader = readerBuilder.build()
  }


  fun run() {
    while (true) {
      try {
        val line = reader.readLine("> ")
        // TODO
      } catch (e: UserInterruptException) { break }
      catch (ee: EndOfFileException) { break }
      catch (ex: Exception) { ex.printStackTrace() }
    }
  }
}