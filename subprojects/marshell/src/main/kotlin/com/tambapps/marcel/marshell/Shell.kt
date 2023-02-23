package com.tambapps.marcel.marshell

import com.tambapps.marcel.marshell.console.ReaderHighlighter
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder

class Shell {

  val reader: LineReader

  init {
    val readerBuilder = LineReaderBuilder.builder()
      .highlighter(ReaderHighlighter())

    reader = readerBuilder.build()
  }


  fun run() {
    while (true) {
      val line = reader.readLine("> ")

    }

  }
}