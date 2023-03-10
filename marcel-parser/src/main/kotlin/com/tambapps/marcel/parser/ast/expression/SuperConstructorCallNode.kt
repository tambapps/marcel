package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

/**
 * Node for a super call in a constructor
 */
class SuperConstructorCallNode constructor(token: LexToken, scope: Scope, arguments: MutableList<ExpressionNode>) : SimpleFunctionCallNode(token, scope, JavaMethod.CONSTRUCTOR_NAME, arguments) {

  override fun <T> accept(astNodeVisitor: AstNodeVisitor<T>) = astNodeVisitor.visit(this)

}