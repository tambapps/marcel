package com.tambapps.marcel.parser.ast

import org.objectweb.asm.MethodVisitor

class DropNode(private val child: ExpressionNode): StatementNode {


  override fun writeInstructions(mv: MethodVisitor) {
    child.writeInstructions(mv)
    // TODO drop what's on the stack if child is expression
  }
}