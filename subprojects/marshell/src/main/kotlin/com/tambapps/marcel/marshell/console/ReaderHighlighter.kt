package com.tambapps.marcel.marshell.console

import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.TokenType.*
import com.tambapps.marcel.marshell.console.style.HighlightStyle
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import java.util.regex.Pattern

class ReaderHighlighter: Highlighter {

  // TODO add coloring on variable/types once parsing will have been implemented
  val lexer = MarcelLexer(false)
  val style = HighlightStyle()

  override fun highlight(reader: LineReader, buffer: String): AttributedString {
    val highlightedString = AttributedStringBuilder()
    val tokens = lexer.lexSafely(buffer)
    tokens.removeLast() // remove end of file
    for (token in tokens) {
      val string = buffer.substring(token.start, token.end)
      val style = when (token.type) {
        TYPE_INT, TYPE_LONG, TYPE_SHORT, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOL, TYPE_BYTE, TYPE_VOID, TYPE_CHAR, FUN, RETURN,
        VALUE_TRUE, VALUE_FALSE, NEW, IMPORT, AS, INLINE, STATIC, FOR, IN, IF, ELSE, NULL, BREAK, CONTINUE, DEF,
        CLASS, EXTENSION, PACKAGE, EXTENDS, IMPLEMENTS, FINAL, SWITCH, WHEN, THIS, SUPER,
          // visibilities
        VISIBILITY_PUBLIC, VISIBILITY_PROTECTED, VISIBILITY_INTERNAL, VISIBILITY_PRIVATE -> style.keyword
        INTEGER, FLOAT -> style.number
        BLOCK_COMMENT, DOC_COMMENT, HASH, SHEBANG_COMMENT, EOL_COMMENT -> style.comment
        LPAR, RPAR -> style.parenthesis
        OPEN_QUOTE, CLOSING_QUOTE, REGULAR_STRING_PART,
        OPEN_CHAR_QUOTE, CLOSING_CHAR_QUOTE,
        OPEN_REGEX_QUOTE, CLOSING_REGEX_QUOTE,
        OPEN_SIMPLE_QUOTE, CLOSING_SIMPLE_QUOTE -> style.string
        SHORT_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_END -> style.stringTemplate
        else -> AttributedStyle.DEFAULT
      }
      highlight(highlightedString, style, string)
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