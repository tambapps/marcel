package com.tambapps.marcel.parser.ast

// TODO might be useless. Just use AstNodeVisitor
interface AstVisitor {

  fun visit(node: AstNode)

}