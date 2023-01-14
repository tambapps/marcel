package com.tambapps.marcel.parser.ast.expression

import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.ast.AstNodeVisitor

interface ExpressionNode: AstNode {

  val type: JavaType
  fun accept(astNodeVisitor: AstNodeVisitor)

}