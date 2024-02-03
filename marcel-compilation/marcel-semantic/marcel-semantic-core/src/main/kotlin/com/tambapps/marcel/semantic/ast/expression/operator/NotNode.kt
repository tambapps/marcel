package com.tambapps.marcel.semantic.ast.expression.operator

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

class NotNode(val expressionNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
              tokenStart: LexToken,
              tokenEnd: LexToken) :
  com.tambapps.marcel.semantic.ast.expression.AbstractExpressionNode(JavaType.boolean, tokenStart, tokenEnd) {

    constructor(expressionNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode): this(expressionNode, expressionNode.tokenStart, expressionNode.tokenEnd)
    constructor(expressionNode: com.tambapps.marcel.semantic.ast.expression.ExpressionNode,
                node: CstNode): this(expressionNode, node.tokenStart, node.tokenEnd)
  override fun <T> accept(visitor: com.tambapps.marcel.semantic.ast.expression.ExpressionNodeVisitor<T>) = visitor.visit(this)

}