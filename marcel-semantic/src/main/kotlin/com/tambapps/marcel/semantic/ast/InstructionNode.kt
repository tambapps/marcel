package com.tambapps.marcel.semantic.ast

interface InstructionNode: Ast2Node {

  fun <T> accept(visitor: AstNodeVisitor<T>): T

}