package com.tambapps.marcel.marshell.console

import org.jline.reader.impl.DefaultParser

class MarshellSnippetParser : DefaultParser() {

    private val wordDelimiters = "."

    override fun isDelimiterChar(buffer: CharSequence, pos: Int): Boolean {
        return wordDelimiters.contains(buffer[pos])
    }
}