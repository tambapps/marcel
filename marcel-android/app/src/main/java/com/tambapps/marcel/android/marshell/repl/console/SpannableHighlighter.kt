package com.tambapps.marcel.android.marshell.repl.console

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.console.AbstractHighlighter
import com.tambapps.marcel.repl.console.HighlightTheme

class SpannableHighlighter(
  replCompiler: MarcelReplCompiler
) :
  AbstractHighlighter<AnnotatedString, AnnotatedString.Builder, Color>(replCompiler, THEME) {

  companion object {
    val THEME = HighlightTheme(
      keyword = Color.Red,
      function = Color.Yellow,
      variable = Color.Magenta,
      string = Color.Green,
      stringTemplate = Color.Yellow,
      number = Color.Cyan,
      comment = Color.LightGray,
      default = Color.White
    )
  }

  override fun newBuilder() = AnnotatedString.Builder()

  override fun build(builder: AnnotatedString.Builder): AnnotatedString = builder.toAnnotatedString()
  override fun highlight(builder: AnnotatedString.Builder, style: Color, string: String) {
    builder.withStyle(SpanStyle(color = style)) {
      append(text = string)
    }
  }
}