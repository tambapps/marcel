package com.tambapps.marcel.parser.ast

import org.objectweb.asm.MethodVisitor

class ConstantValueNode(private val type: TokenNodeType, private val value: String): ExpressionNode {

  override fun writeInstructions(mv: MethodVisitor) {
    when (type) {
      TokenNodeType.INTEGER -> {
        mv.visitLdcInsn(value.toInt()) // write primitive value, from an Object class e.g. Integer -> int
      }
      else -> throw UnsupportedOperationException("Cannot handle values of type $type yet")
    }
  }
}