package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.statement.BlockCstNode
import com.tambapps.marcel.parser.cst.visitor.ExpressionCstNodeVisitor

class LambdaCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  val parameters: List<MethodParameterCstNode>,
  val blockCstNode: BlockCstNode,
  val explicit0Parameters: Boolean,
) : AbstractExpressionCstNode(parent, tokenStart, tokenEnd) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is LambdaCstNode) return false

    if (explicit0Parameters != other.explicit0Parameters) return false
    if (parameters != other.parameters) return false
    if (blockCstNode != other.blockCstNode) return false

    return true
  }

  override fun hashCode(): Int {
    var result = explicit0Parameters.hashCode()
    result = 31 * result + parameters.hashCode()
    result = 31 * result + blockCstNode.hashCode()
    return result
  }

  class MethodParameterCstNode(
    parent: CstNode?,
    tokenStart: LexToken,
    tokenEnd: LexToken,
    val type: TypeCstNode?,
    val name: String,
    val nullable: Boolean,
  ) : AbstractCstNode(parent, tokenStart, tokenEnd) {

    override fun toString(): String {
      return if (type != null) "$type $name" else name
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is MethodParameterCstNode) return false

      if (nullable != other.nullable) return false
      if (type != other.type) return false
      if (name != other.name) return false

      return true
    }

    override fun hashCode(): Int {
      var result = nullable.hashCode()
      result = 31 * result + (type?.hashCode() ?: 0)
      result = 31 * result + name.hashCode()
      return result
    }


  }
}