package com.tambapps.marcel.android.marshell.repl.console

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.console.AbstractHighlighter
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

class SpannableHighlighter(typeResolver: MarcelSymbolResolver,
                           replCompiler: MarcelReplCompiler
) :
  AbstractHighlighter<AnnotatedString.Builder, Color>(typeResolver, replCompiler) {

  override val keywordStyle = Color.Red
  override val functionStyle = Color.Yellow
  override val variableStyle = Color.Magenta
  override val stringStyle = Color.Green
  override val stringTemplateStyle = Color.Yellow
  override val numberStyle = Color.Cyan
  override val commentStyle = Color.LightGray
  override val defaultStyle: Color get() = Color.White

  override fun newHighlightedString(text: CharSequence): AnnotatedString.Builder {
    return AnnotatedString.Builder().append(text)
  }

  override fun highlight(highlightedString: AnnotatedString.Builder, style: Color, string: String,
                         startIndex: Int, endIndex: Int) {
    highlightedString.withStyle(SpanStyle(color = style)) {
      append(text = string)
    }
  }
}