package com.tambapps.marcel.parser.ast

import org.objectweb.asm.MethodVisitor

data class IntConstantNode(private val value: Int): ExpressionNode {

  override fun writeInstructions(mv: MethodVisitor) {
    mv.visitLdcInsn(value) // write primitive value, from an Object class e.g. Integer -> int
  }
}