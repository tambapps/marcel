package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod

/**
 * Node for a super call in a constructor
 */
class ThisConstructorCallNode constructor(token: LexToken, scope: Scope, arguments: MutableList<ExpressionNode>) : SimpleFunctionCallNode(token, scope, JavaMethod.CONSTRUCTOR_NAME, arguments) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

  override fun doGetMethod(typeResolver: AstNodeTypeResolver): JavaMethod {
    val type = scope.classType.superType ?: throw MarcelSemanticException(token, "Cannot call super constructor from a type with no parent")
    return typeResolver.findMethodOrThrow(type, name, arguments.map { it.accept(typeResolver) }, this)
  }
}