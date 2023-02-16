package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.visitor.CheckAllPathsReturnVisitor
import com.tambapps.marcel.parser.ast.visitor.ForEachNodeVisitor
import com.tambapps.marcel.parser.scope.Scope

interface AstInstructionNode: AstNode {

  fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T
  fun allBranchesReturn(): Boolean = accept(CheckAllPathsReturnVisitor())

  fun setTreeScope(scope: Scope) {
    accept(ForEachNodeVisitor {
      if (it is ScopedNode<*>) it.trySetScope(scope)
    })
  }
}