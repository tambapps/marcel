package com.tambapps.marcel.parser.ast

interface AstNode {

  fun accept(visitor: AstVisitor) {
    visitor.visit(this)
  }
}