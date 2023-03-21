package com.tambapps.marcel.marshell.console

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.marshell.console.style.HighlightTheme
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.console.AbstractHighlighter
import org.jline.reader.Highlighter
import org.jline.reader.LineReader
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStringBuilder
import org.jline.utils.AttributedStyle
import java.util.regex.Pattern

class ReaderHighlighter constructor(
  typeResolver: JavaTypeResolver,
  replCompiler: MarcelReplCompiler
): AbstractHighlighter<AttributedStringBuilder, AttributedStyle>(typeResolver, replCompiler), Highlighter {

  private val style = HighlightTheme()
  override val variableStyle: AttributedStyle = style.variable
  override val functionStyle: AttributedStyle = style.function
  override val stringStyle: AttributedStyle = style.string
  override val stringTemplateStyle: AttributedStyle = style.stringTemplate
  override val keywordStyle: AttributedStyle = style.keyword
  override val commentStyle: AttributedStyle = style.comment
  override val numberStyle: AttributedStyle = style.number
  override val defaultStyle: AttributedStyle = AttributedStyle.DEFAULT

  override fun highlight(reader: LineReader?, buffer: String): AttributedString {
    return highlight(buffer).toAttributedString()
  }

  override fun newHighlightedString(text: String): AttributedStringBuilder {
    return AttributedStringBuilder()
  }

  override fun highlight(highlightedString: AttributedStringBuilder, style: AttributedStyle, string: String,
                         startIndex: Int, endIndex: Int) {
    highlightedString.style(style)
    highlightedString.append(string)
  }

  override fun setErrorPattern(errorPattern: Pattern) {}

  override fun setErrorIndex(errorIndex: Int) {}

}