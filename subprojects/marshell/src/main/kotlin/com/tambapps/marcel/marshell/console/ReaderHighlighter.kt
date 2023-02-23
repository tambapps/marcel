package com.tambapps.marcel.marshell.console

import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.marshell.console.style.HighlightStyle
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import java.util.regex.Pattern

class ReaderHighlighter: Highlighter {

  val lexer = MarcelLexer(false)
  val style = HighlightStyle()

  override fun highlight(reader: LineReader, buffer: String): AttributedString {
    val highlightedString = AttributedStringBuilder()
    val tokens = lexer.lexSafely(buffer)
    for (token in tokens) {
      val string = buffer.substring(token.start, token.end)
      when (token.type) {
        TokenType.TYPE_INT -> highlight(highlightedString, style.keyword, string)
        TokenType.END_OF_FILE -> {}
        else -> highlight(highlightedString, AttributedStyle.DEFAULT, string)
      }
    }
    return highlightedString.toAttributedString()
  }

  private fun highlight(builder: AttributedStringBuilder, style: AttributedStyle, string: String) {
    builder.style(style)
    builder.append(string)
  }


  override fun setErrorPattern(errorPattern: Pattern) {}

  override fun setErrorIndex(errorIndex: Int) {}

}