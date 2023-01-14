package com.tambapps.marcel.parser.ast.statement

import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.visitor.StatementVisitor

interface StatementNode: AstNode {

  val expressionType: JavaType
  fun accept(mv: StatementVisitor)

}