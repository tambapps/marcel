package com.tambapps.marcel.android.marshell.repl.console

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.repl.MarcelReplCompiler
import com.tambapps.marcel.repl.console.AbstractHighlighter

// style is color
class TextViewHighlighter(typeResolver: JavaTypeResolver,
                          replCompiler: MarcelReplCompiler) :
  AbstractHighlighter<Spannable, Int>(typeResolver, replCompiler) {

  override val keywordStyle = Color.RED
  override val functionStyle = Color.YELLOW
  override val variableStyle = Color.MAGENTA
  override val stringStyle = Color.GREEN
  override val stringTemplateStyle = Color.YELLOW
  override val numberStyle = Color.CYAN
  override val commentStyle = Color.LTGRAY
  override val defaultStyle: Int get() = Color.WHITE

  override fun newHighlightedString(text: CharSequence): Spannable {
    return if (text is Spannable) text else SpannableString(text)
  }

  override fun highlight(highlightedString: Spannable, style: Int, string: String,
                         startIndex: Int, endIndex: Int) {
    highlightedString.setSpan(ForegroundColorSpan(style), startIndex, endIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
  }
}