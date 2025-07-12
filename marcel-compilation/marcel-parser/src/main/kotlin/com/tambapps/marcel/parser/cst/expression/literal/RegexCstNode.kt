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

  override fun toString() = "/$value/"
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is RegexCstNode) return false

    if (value != other.value) return false
    if (flags != other.flags) return false

    return true
  }

  override fun hashCode(): Int {
    var result = value.hashCode()
    result = 31 * result + flags.hashCode()
    return result
  }

}