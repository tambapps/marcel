package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ScopedNode
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.scope.Scope

open class MultiVariableDeclarationNode(token: LexToken, override var scope: Scope,
                                        val declarations: List<Pair<JavaType, String>?>,
                                        val expression: ExpressionNode
): AbstractStatementNode(token), ScopedNode<Scope> {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return declarations.joinToString(separator = ", ", prefix = "(", postfix = ") = ", transform = {
      if (it != null) "${it.first} ${it.second}"
      else "_"
    }) +
        expression
  }

}