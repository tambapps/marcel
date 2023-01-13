package com.tambapps.marcel.parser.ast

import org.objectweb.asm.MethodVisitor

interface ExpressionNode: AstNode {
  fun writeInstructions(mv: MethodVisitor)
}