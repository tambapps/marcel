package com.tambapps.marcel.parser.ast

interface AstNode {


  // TODO remove this method
  fun accept(visitor: AstVisitor) {
    visitor.visit(this)
  }
}