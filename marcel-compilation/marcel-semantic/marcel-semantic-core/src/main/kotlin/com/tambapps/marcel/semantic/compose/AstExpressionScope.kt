package com.tambapps.marcel.semantic.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.Variable

open class AstExpressionScope(
  val tokenStart: LexToken = LexToken.DUMMY,
  val tokenEnd: LexToken = LexToken.DUMMY,
) {


  fun nullValue(type: JavaType? = null) = NullValueNode(tokenStart, type)
  fun int(value: Int) = IntConstantNode(tokenStart, value)
  fun long(value: Long) = LongConstantNode(tokenStart, value)
  fun float(value: Float) = FloatConstantNode(tokenStart, value)
  fun double(value: Double) = DoubleConstantNode(tokenStart, value)
  fun char(value: Char) = CharConstantNode(tokenStart, value)
  fun string(value: String) = StringConstantNode(value, tokenStart, tokenEnd)

  fun lv(type: JavaType, name: String, index: Int) = LocalVariable(type, name, type.nbSlots, index, false, Nullness.UNKNOWN)
  fun ref(variable: Variable, owner: ExpressionNode? = null) = ReferenceNode(owner, variable, tokenStart)
}
