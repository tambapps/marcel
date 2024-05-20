package com.tambapps.marcel.android.marshell.repl.console

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.console.AbstractHighlighter
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

class SpannableHighlighter(
  replCompiler: MarcelReplCompiler
) :
  AbstractHighlighter<AnnotatedString, AnnotatedString.Builder, Color>(replCompiler) {

  override val keywordStyle = Color.Red
  override val functionStyle = Color.Yellow
  override val variableStyle = Color.Magenta
  override val stringStyle = Color.Green
  override val stringTemplateStyle = Color.Yellow
  override val numberStyle = Color.Cyan
  override val commentStyle = Color.LightGray
  override val defaultStyle: Color get() = Color.White

  override fun newBuilder() = AnnotatedString.Builder()

  override fun build(builder: AnnotatedString.Builder): AnnotatedString = builder.toAnnotatedString()
  override fun highlight(builder: AnnotatedString.Builder, style: Color, string: String) {
    builder.withStyle(SpanStyle(color = style)) {
      append(text = string)
    }
  }
}