package com.tambapps.marcel.parser.cst.expression.literal

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.expression.AbstractExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor
import java.util.regex.Pattern

class RegexCstNode(
  parent: CstNode? = null,
  override val value: String,
  val flags: List<Int>,
   tokenStart: LexToken,
  tokenEnd: LexToken
)
  : AbstractExpressionCstNode(parent, tokenStart, tokenEnd),
  ExpressionCstNode {
  companion object {
    val FLAGS_MAP = mapOf(
      Pair('d', Pattern.UNIX_LINES),
      Pair('i', Pattern.CASE_INSENSITIVE),
      Pair('x', Pattern.COMMENTS),
      Pair('m', Pattern.MULTILINE),
      Pair('l', Pattern.LITERAL),
      Pair('s', Pattern.DOTALL),
      Pair('u', Pattern.UNICODE_CASE),
      Pair('c', Pattern.CANON_EQ),
      Pair('U', Pattern.UNICODE_CHARACTER_CLASS),
    )
  }

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  override fun toString() = buildString {
    append("r/$value/")
    if (flags.isNotEmpty()) {
      flags.joinTo(buffer = this, separator = "") { char ->
        FLAGS_MAP.entries.find { it.value == char }?.key?.toString() ?: "?"
      }
    }
  }
}