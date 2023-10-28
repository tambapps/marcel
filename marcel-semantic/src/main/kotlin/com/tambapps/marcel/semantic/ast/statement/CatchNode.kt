package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable

class CatchNode(
  val throwableTypes: List<JavaType>,
  val throwableVariable: LocalVariable,
  val statement: StatementNode
) {
}