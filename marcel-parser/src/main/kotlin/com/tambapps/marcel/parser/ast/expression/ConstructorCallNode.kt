package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

/**
 * Node for new MyClass()
 */
class ConstructorCallNode(token: LexToken, scope: Scope, val type: JavaType, arguments: MutableList<ExpressionNode>): SimpleFunctionCallNode(token, scope, JavaMethod.CONSTRUCTOR_NAME, arguments) {

  init {
    methodOwnerType = this
  }

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "new $type(" + arguments.joinToString(separator = ",") + ")"
  }
}

class NamedParametersConstructorCallNode(token: LexToken, scope: Scope, val type: JavaType, namedArguments: List<NamedArgument>): NamedParametersFunctionCall(token, scope, JavaMethod.CONSTRUCTOR_NAME, emptyList(),  namedArguments) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun toString(): String {
    return "new $type(" + namedArguments.joinToString(separator = ",", transform = { "${it.name}: ${it.valueExpression}"}) + ")"
  }
}
