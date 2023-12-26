package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode

class LambdaCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val parameters: List<MethodParameterCstNode>,
  val blockCstNode: BlockCstNode,
  val explicit0Parameters: Boolean,
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

  class MethodParameterCstNode(
    parent: CstNode?,
    tokenStart: LexToken,
    tokenEnd: LexToken,
    val type: TypeCstNode?,
    val name: String,
  ) : AbstractCstNode(parent, tokenStart, tokenEnd) {

    override fun toString(): String {
      return if (type != null) "$type $name" else name
    }
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false
      if (!super.equals(other)) return false

      other as MethodParameterCstNode

      if (type != other.type) return false
      if (name != other.name) return false

      return true
    }

    override fun hashCode(): Int {
      var result = super.hashCode()
      result = 31 * result + (type?.hashCode() ?: 0)
      result = 31 * result + name.hashCode()
      return result
    }

  }
}