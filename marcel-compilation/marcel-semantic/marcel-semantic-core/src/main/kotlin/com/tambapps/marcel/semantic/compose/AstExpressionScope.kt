package com.tambapps.marcel.semantic.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.TernaryNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.GeNode
import com.tambapps.marcel.semantic.ast.expression.operator.GtNode
import com.tambapps.marcel.semantic.ast.expression.operator.IncrNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeNode
import com.tambapps.marcel.semantic.ast.expression.operator.LtNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.ModNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.NotNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.symbol.method.MarcelMethod
import com.tambapps.marcel.semantic.symbol.type.JavaArrayType
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.Variable

open class AstExpressionScope(
  val tokenStart: LexToken = LexToken.DUMMY,
  val tokenEnd: LexToken = LexToken.DUMMY,
) {

  fun not(expr: ExpressionNode) = NotNode(expr, tokenStart, tokenEnd)

  fun minus(expr1: ExpressionNode, expr2: ExpressionNode) = MinusNode(expr1, expr2)
  fun plus(expr1: ExpressionNode, expr2: ExpressionNode) = PlusNode(expr1, expr2)
  fun div(expr1: ExpressionNode, expr2: ExpressionNode) = DivNode(expr1, expr2)
  fun mul(expr1: ExpressionNode, expr2: ExpressionNode) = MulNode(expr1, expr2)
  fun mod(expr1: ExpressionNode, expr2: ExpressionNode) = ModNode(expr1, expr2)
  fun gt(expr1: ExpressionNode, expr2: ExpressionNode) = GtNode(expr1, expr2)
  fun ge(expr1: ExpressionNode, expr2: ExpressionNode) = GeNode(expr1, expr2)
  fun lt(expr1: ExpressionNode, expr2: ExpressionNode) = LtNode(expr1, expr2)
  fun le(expr1: ExpressionNode, expr2: ExpressionNode) = LeNode(expr1, expr2)
  fun eq(expr1: ExpressionNode, expr2: ExpressionNode) = LeNode(expr1, expr2)

  fun classRef(type: JavaType) = ClassReferenceNode(type, tokenStart)
  fun thisRef(type: JavaType) = ThisReferenceNode(type, tokenStart, )
  fun superRef(type: JavaType) = SuperReferenceNode(type, tokenStart, )

  fun ternary(testExpr: ExpressionNode, trueExpr: ExpressionNode, falseExpr: ExpressionNode) =
    TernaryNode(testExpr, trueExpr, falseExpr, tokenStart, tokenEnd)
  fun instanceof(expr: ExpressionNode, type: JavaType) = InstanceOfNode(type, expr, tokenStart, tokenEnd)

  fun fCall(method: MarcelMethod, args: List<ExpressionNode> = emptyList(), owner: ExpressionNode? = null) =
    FunctionCallNode(
      method, owner, args, tokenStart, tokenEnd,
    )

  fun nullValue(type: JavaType? = null) = NullValueNode(tokenStart, type)
  fun int(value: Int) = IntConstantNode(tokenStart, value)
  fun bool(value: Boolean) = BoolConstantNode(tokenStart, value)
  fun long(value: Long) = LongConstantNode(tokenStart, value)
  fun float(value: Float) = FloatConstantNode(tokenStart, value)
  fun double(value: Double) = DoubleConstantNode(tokenStart, value)
  fun char(value: Char) = CharConstantNode(tokenStart, value)
  fun string(value: String) = StringConstantNode(value, tokenStart, tokenEnd)

  fun ref(variable: Variable, owner: ExpressionNode? = null) = ReferenceNode(owner, variable, tokenStart)

  fun array(type: JavaArrayType, vararg expr: ExpressionNode) = ArrayNode(expr.toMutableList(), type = type, tokenStart = tokenStart, tokenEnd = tokenEnd)

  fun lv(type: JavaType, name: String, index: Int = name.hashCode(), nullness: Nullness = if (type.primitive) Nullness.NOT_NULL else Nullness.UNKNOWN) = LocalVariable(
    type, name, type.nbSlots, index, isFinal = false, nullness = nullness
  )

  fun arrayAccess(expr: ExpressionNode, indexExpr: ExpressionNode) = ArrayAccessNode(
    expr, indexExpr, expr.tokenStart, indexExpr.tokenEnd
  )
  fun map(vararg pairs: Pair<ExpressionNode, ExpressionNode>) = MapNode(
    pairs.toList(),
    tokenStart = tokenStart,
    tokenEnd = tokenEnd
  )

}
