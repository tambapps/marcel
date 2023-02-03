package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.AstTypedObject

interface ExpressionNode: AstNode {

  fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T

}