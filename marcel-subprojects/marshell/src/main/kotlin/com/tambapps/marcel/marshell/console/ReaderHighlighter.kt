package com.tambapps.marcel.marshell.console

import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.console.AbstractHighlighter
import com.tambapps.marcel.repl.console.HighlightTheme
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import java.util.regex.Pattern

class ReaderHighlighter constructor(
  replCompiler: MarcelReplCompiler
): AbstractHighlighter<AttributedString, AttributedStringBuilder, AttributedStyle>(
  replCompiler, THEME
), Highlighter {

  companion object {
    val THEME = HighlightTheme(
      keyword = AttributedStyle.BOLD.foreground(AttributedStyle.RED),
      function = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW),
      field = AttributedStyle.DEFAULT.foreground(AttributedStyle.MAGENTA),
      string = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN),
      stringTemplate = AttributedStyle.BOLD.foreground(
        AttributedStyle.YELLOW
      ),
      annotation = AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW),
      number = AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN),
      comment = AttributedStyle.DEFAULT.foreground(AttributedStyle.BRIGHT),
      default = AttributedStyle.DEFAULT,
      variable = AttributedStyle.DEFAULT,
    )
  }

  override fun highlight(reader: LineReader?, buffer: String) = highlight(buffer)

  override fun newBuilder() = AttributedStringBuilder()

  override fun highlight(builder: AttributedStringBuilder, style: AttributedStyle, string: String) {
    builder.style(style)
    builder.append(string)
  }

  override fun build(builder: AttributedStringBuilder): AttributedString = builder.toAttributedString()

  override fun setErrorPattern(errorPattern: Pattern) {}

  override fun setErrorIndex(errorIndex: Int) {}

}