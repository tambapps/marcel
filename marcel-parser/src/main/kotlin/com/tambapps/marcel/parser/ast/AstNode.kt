package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.scope.Scope

interface AstNode {

  fun trySetTreeScope(scope: Scope) {
    accept(object : AstVisitor {
      override fun visit(node: AstNode) {
        if (node is ScopedNode<*>) node.trySetScope(scope)
      }
    })
  }

  fun accept(visitor: AstVisitor) {
    visitor.visit(this)
  }
}