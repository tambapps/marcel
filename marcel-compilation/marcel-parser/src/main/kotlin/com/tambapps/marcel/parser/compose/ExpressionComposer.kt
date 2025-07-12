package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.parser.cst.expression.LambdaCstNode
import com.tambapps.marcel.parser.cst.expression.NotCstNode
import com.tambapps.marcel.parser.cst.expression.TemplateStringCstNode
import com.tambapps.marcel.parser.cst.expression.UnaryMinusCstNode
import com.tambapps.marcel.parser.cst.expression.literal.DoubleCstNode
import com.tambapps.marcel.parser.cst.expression.literal.FloatCstNode
import com.tambapps.marcel.parser.cst.expression.literal.IntCstNode
import com.tambapps.marcel.parser.cst.expression.literal.LongCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.literal.StringCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ClassReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.IndexAccessCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode

open class ExpressionComposer(
  val tokenStart: LexToken = LexToken.DUMMY,
  val tokenEnd: LexToken = LexToken.DUMMY,
) {

  companion object {
    fun compose(composer: ExpressionComposer.() -> ExpressionCstNode) = composer.invoke(ExpressionComposer())
  }

  fun fCall(value: String, castType: TypeCstNode? = null, positionalArgumentNodes: List<ExpressionCstNode> = emptyList(),
                    namedArgumentNodes: List<Pair<String, ExpressionCstNode>> = emptyList()
  ) = FunctionCallCstNode(parent = null, value = value, castType = castType,
    positionalArgumentNodes = positionalArgumentNodes, namedArgumentNodes = namedArgumentNodes,
    tokenStart = tokenStart, tokenEnd = tokenEnd
  )

  fun minus(expr: ExpressionCstNode) = UnaryMinusCstNode(expr, null, tokenStart, tokenEnd)
  fun not(expr: ExpressionCstNode) = NotCstNode(expr, null, tokenStart, tokenEnd)

  fun and(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.AND, left, right)
  fun plus(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.PLUS, left, right)
  fun mul(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.MUL, left, right)
  fun dot(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.DOT, left, right)
  fun isEqual(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.EQUAL, left, right)
  fun gt(left: ExpressionCstNode, right: ExpressionCstNode) = binaryOperator(TokenType.GT, left, right)
  private fun binaryOperator(type: TokenType, left: ExpressionCstNode, right: ExpressionCstNode) =
    BinaryOperatorCstNode(type, left, right, null, tokenStart, tokenEnd)
  fun indexAccess(owner: ExpressionCstNode, indexes: List<ExpressionCstNode>, isSafeAccess: Boolean = false) =
    IndexAccessCstNode(null, owner, indexes, isSafeAccess, tokenStart, tokenEnd)
  fun nullValue() = NullCstNode(token = tokenStart)
  fun type(value: String, genericTypes: List<TypeCstNode> = emptyList(), arrayDimensions: Int = 0) = TypeCstNode(null, value, genericTypes, arrayDimensions, tokenStart, tokenEnd)
  fun int(value: Int) = IntCstNode(value = value, token = tokenStart)
  fun string(value: Any) = StringCstNode(value = value.toString(), token = tokenStart)
  fun templateSting(value: Any) = TemplateStringCstNode(expressions = listOf(string(value)), tokenStart = tokenStart, tokenEnd = tokenEnd, parent = null)
  fun float(value: Float) = FloatCstNode(value = value, token = tokenStart)
  fun long(value: Long) = LongCstNode(value = value, token = tokenStart)
  fun double(value: Double) = DoubleCstNode(value = value, token = tokenStart)
  fun ref(name: String) = ReferenceCstNode(value = name, token = tokenStart, parent = null)
  fun lambdaParam(type: TypeCstNode? = null, name: String, isNullable: Boolean = false) = LambdaCstNode.MethodParameterCstNode(null, tokenStart, tokenEnd, type, name, isNullable)
  fun classReference(type: TypeCstNode) = ClassReferenceCstNode(null, type, tokenStart, tokenEnd)

}