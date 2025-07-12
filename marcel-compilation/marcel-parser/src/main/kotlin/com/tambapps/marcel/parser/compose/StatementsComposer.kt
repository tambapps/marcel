package com.tambapps.marcel.parser.compose

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.parser.cst.expression.ExpressionCstNode
import com.tambapps.marcel.parser.cst.statement.ExpressionStatementCstNode
import com.tambapps.marcel.parser.cst.statement.ReturnCstNode
import com.tambapps.marcel.parser.cst.statement.VariableDeclarationCstNode

open class StatementsComposer(
  tokenStart: LexToken = LexToken.DUMMY,
  tokenEnd: LexToken = LexToken.DUMMY
) :
  ExpressionComposer(tokenStart, tokenEnd) {

  fun stmt(expr: ExpressionCstNode) = ExpressionStatementCstNode(expr)
  fun varDecl(typeNode: TypeCstNode, name: String, expr: ExpressionCstNode?, isNullable: Boolean = false) = VariableDeclarationCstNode(typeNode,
    LexToken.dummy(name), expr, isNullable, null, tokenStart, tokenEnd)
  fun returnNode(expr: ExpressionCstNode? = null) = ReturnCstNode(expressionNode = expr, tokenStart = tokenStart, tokenEnd = tokenEnd)

}