package com.tambapps.marcel.parser.ast

import org.objectweb.asm.MethodVisitor

interface StatementNode: AstNode {

  fun writeInstructions(mv: MethodVisitor)

}