package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.ast.BinaryOperatorNode
import com.tambapps.marcel.parser.ast.FunctionCallNode
import com.tambapps.marcel.parser.ast.IntConstantNode
import com.tambapps.marcel.parser.ast.TernaryNode
import com.tambapps.marcel.parser.visitor.ExpressionVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ExpressionGenerator(private val mv: MethodVisitor): ExpressionVisitor {


  override fun visit(integer: IntConstantNode) {
    mv.visitLdcInsn(integer.value) // write primitive value, from an Object class e.g. Integer -> int
  }

  override fun visit(binaryOperatorNode: BinaryOperatorNode) {
    TODO("Not yet implemented")
  }

  override fun visit(ternaryNode: TernaryNode) {
    TODO("Not yet implemented")
  }

  override fun visit(functionCallNode: FunctionCallNode) {
    if (functionCallNode.name == "println") {
      mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
    } else {
      throw UnsupportedOperationException("Cannot handle function call yet")
    }
    // TODO add a type field and check types when calling functions
    for (argumentNode in functionCallNode.arguments) {
      // write argument on the stack
      argumentNode.accept(this)
    }
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false)
  }

}