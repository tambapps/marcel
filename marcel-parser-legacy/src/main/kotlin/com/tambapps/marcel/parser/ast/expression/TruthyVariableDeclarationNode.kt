package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType

class TruthyVariableDeclarationNode(token: LexToken, scope: Scope, val variableType: JavaType, name: String, expression: ExpressionNode) :
  VariableDeclarationNode(token, scope, JavaType.boolean, name, false, expression) {


  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "truthy(${variableType.className} $name = $expression)"
  }
}