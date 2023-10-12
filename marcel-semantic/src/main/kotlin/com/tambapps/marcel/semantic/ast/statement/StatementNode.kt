package com.tambapps.marcel.semantic.ast.statement

import com.tambapps.marcel.semantic.ast.Ast2Node
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.InstructionNode

interface StatementNode: Ast2Node, InstructionNode {

  fun <T> accept(visitor: StatementNodeVisitor<T>): T

}