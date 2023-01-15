package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.TypedNode

interface ExpressionNode: AstNode, TypedNode {

  fun accept(astNodeVisitor: AstNodeVisitor)

}