package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaPrimitiveType
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.Variable

class IncrNode(
  token: LexToken,
  val variable: Variable,
  val tempVariable: LocalVariable?, // can be useful when value needs to be pushed (owner != null)
  val owner: ExpressionNode?,
  val amount: Any, // long, int, byte, float or double
  val primitiveType: JavaPrimitiveType,
  val returnValueBefore: Boolean,
) : AbstractExpressionNode(variable.type, token) {

  // constructor for local variables
  constructor(
    token: LexToken,
    lv: LocalVariable,
    amount: Any,
    primitiveType: JavaPrimitiveType,
    returnValueBefore: Boolean
  ) :
      this(
        token, variable = lv, tempVariable = null,
        null, amount, primitiveType, returnValueBefore
      )

  override fun <T> accept(visitor: ExpressionNodeVisitor<T>) =
    visitor.visit(this)
}