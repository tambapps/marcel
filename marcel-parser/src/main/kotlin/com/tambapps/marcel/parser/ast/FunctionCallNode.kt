package com.tambapps.marcel.parser.ast

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class FunctionCallNode(val name: String, val arguments: MutableList<ExpressionNode>): ExpressionNode {

  constructor(name: String): this(name, mutableListOf())

  override fun writeInstructions(mv: MethodVisitor) {
    if (name == "println") {
      mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
    } else {
      throw UnsupportedOperationException("Cannot handle function call yet")
    }
    // TODO add a type field and check types when calling functions
    for (argumentNode in arguments) {
      // write argument on the stack
      argumentNode.writeInstructions(mv)
    }
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false)
  }
}