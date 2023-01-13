package com.tambapps.marcel.parser.ast

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes


data class FunctionCallNode(val name: String) : TokenNodeWithChild<TokenNode>(TokenNodeType.FUNCTION_CALL), Statement {


  override fun write(visitor: MethodVisitor) {
    visitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
    if (true) { // number
      // TODO pass integer as argument
      // TODO doesn't work for big values  "pushes an byte (integer) value to the stack"
      visitor.visitIntInsn(Opcodes.BIPUSH, 8);
      visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
    } else if (false) { // string
    //  visitor.visitVarInsn(ALOAD, id);
      visitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }
  }

}