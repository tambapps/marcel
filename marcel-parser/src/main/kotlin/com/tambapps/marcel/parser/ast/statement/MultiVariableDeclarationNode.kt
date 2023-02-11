package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.scope.Scope

open class MultiVariableDeclarationNode(val scope: Scope,
                                        val declarations: List<Pair<JavaType, String>>,
                                        override val expression: ExpressionNode
): StatementNode {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return declarations.joinToString(separator = ", ", prefix = "(", postfix = ") = ", transform = { "${it.first} ${it.second}" }) +
        expression
  }

}