package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.visitor.CheckAllPathsReturnVisitor
import com.tambapps.marcel.parser.ast.visitor.FindNodeVisitor
import com.tambapps.marcel.parser.ast.visitor.ForEachNodeVisitor
import com.tambapps.marcel.parser.scope.Scope

interface AstInstructionNode: AstNode {

  fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T
  fun allBranchesReturn(): Boolean = accept(CheckAllPathsReturnVisitor())

  fun forEachNode(consumer: (AstNode) -> Unit) {
    accept(ForEachNodeVisitor(consumer))
  }

  fun find(predicate: (AstNode) -> Boolean): AstNode? {
    return accept(FindNodeVisitor(predicate))
  }

  fun setTreeScope(scope: Scope) {
    forEachNode {
      if (it is ScopedNode<*>) it.trySetScope(scope)
    }
  }
}