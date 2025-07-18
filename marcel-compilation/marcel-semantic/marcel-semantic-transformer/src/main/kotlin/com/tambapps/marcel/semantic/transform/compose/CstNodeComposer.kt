package com.tambapps.marcel.semantic.transform.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.parser.compose.BlockStatementScope
import com.tambapps.marcel.parser.compose.StatementScope
import com.tambapps.marcel.parser.cst.AccessCstNode
import com.tambapps.marcel.parser.cst.AnnotationCstNode
import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.BinaryOperatorCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.expression.literal.NullCstNode
import com.tambapps.marcel.parser.cst.expression.reference.DirectFieldReferenceCstNode
import com.tambapps.marcel.parser.cst.expression.reference.ReferenceCstNode
import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.symbol.type.JavaType
import kotlin.reflect.KClass

open class CstNodeComposer {

  protected fun methodNode(
    classNode: ClassCstNode,
    accessNode: AccessCstNode,
    name: String,
    parameters: List<MethodParameterCstNode> = emptyList(),
    annotations: List<AnnotationCstNode> = emptyList(),
    returnType: TypeCstNode,
    tokenStart: LexToken = LexToken.DUMMY,
    tokenEnd: LexToken = LexToken.DUMMY,
    isReturnTypeNullable: Boolean = false,
    statementsSupplier: StatementScope.() -> Unit,
  ): MethodCstNode {

    val methodNode = MethodCstNode(
      classNode, tokenStart, tokenEnd,
      accessNode, name, returnType, isReturnTypeNullable,
    )
    methodNode.parameters.addAll(parameters)
    methodNode.annotations.addAll(annotations)
    val statementsComposer = BlockStatementScope(methodNode.tokenStart, methodNode.tokenEnd, methodNode, methodNode.statements)
    statementsSupplier.invoke(statementsComposer)
    return methodNode
  }

  protected fun visibility(visibility: Visibility): TokenType = when (visibility) {
    Visibility.PUBLIC -> TokenType.VISIBILITY_PUBLIC
    Visibility.PROTECTED -> TokenType.VISIBILITY_PROTECTED
    Visibility.INTERNAL -> TokenType.VISIBILITY_INTERNAL
    Visibility.PRIVATE -> TokenType.VISIBILITY_PRIVATE
  }

  protected fun access(parent: CstNode, isStatic: Boolean = false, isFinal: Boolean = false, isExplicit: Boolean = true,
                       visibility: Visibility = Visibility.PUBLIC) =
    access(parent = parent, isStatic = isStatic, isFinal = isFinal, isExplicit = isExplicit, visibility = visibility(visibility))

  protected fun access(parent: CstNode, isStatic: Boolean = false, isFinal: Boolean = false, isExplicit: Boolean = true,
                       visibility: TokenType) = AccessCstNode(
    parent = parent,
    tokenStart = LexToken.DUMMY,
    tokenEnd = LexToken.DUMMY,
    isStatic = isStatic,
    isFinal = isFinal,
    isInline = false,
    visibility = visibility,
    isExplicit = isExplicit
  )

  protected fun access(node: ClassCstNode, isStatic: Boolean = false, isFinal: Boolean = false, isExplicit: Boolean = true,
                       visibility: TokenType) = AccessCstNode(
    parent = node,
    tokenStart = LexToken.DUMMY,
    tokenEnd = LexToken.DUMMY,
    isStatic = isStatic,
    isFinal = isFinal || node.isEnum,
    isInline = false,
    visibility = visibility,
    isExplicit = isExplicit
  )

  protected fun type(type: KClass<*>): TypeCstNode = type(type.javaType)
  protected fun type(type: JavaType): TypeCstNode =
    TypeCstNode(null, type.className, emptyList(), 0, LexToken.DUMMY, LexToken.DUMMY)

  protected fun ref(name: String) = ReferenceCstNode(null, name, LexToken.DUMMY)
  protected fun directFieldRef(name: String) = DirectFieldReferenceCstNode(null, name, LexToken.DUMMY)

  protected fun isNull(expr: ExpressionCstNode) = equal(expr, NullCstNode(null, LexToken.DUMMY))

  protected fun varAssign(name: String, expr: ExpressionCstNode): ExpressionCstNode {
    return binaryOperator(
      tokenType = TokenType.ASSIGNMENT,
      ReferenceCstNode(parent = null, value = name, token = LexToken.DUMMY),
      expr
    )
  }

  private fun binaryOperator(tokenType: TokenType, expr1: ExpressionCstNode, expr2: ExpressionCstNode) =
    BinaryOperatorCstNode(
      tokenType, expr1, expr2, null, LexToken.DUMMY, LexToken.DUMMY
    )

  protected fun equal(expr1: ExpressionCstNode, expr2: ExpressionCstNode) = binaryOperator(
    TokenType.EQUAL,
    expr1,
    expr2,
  )

}