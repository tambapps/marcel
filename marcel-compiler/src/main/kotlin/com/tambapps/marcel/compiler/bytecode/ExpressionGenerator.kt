package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.operator.binary.BinaryOperatorNode
import com.tambapps.marcel.parser.ast.operator.binary.DivOperator
import com.tambapps.marcel.parser.ast.operator.binary.MinusOperator
import com.tambapps.marcel.parser.ast.operator.binary.MulOperator
import com.tambapps.marcel.parser.ast.operator.binary.PlusOperator
import com.tambapps.marcel.parser.ast.operator.binary.PowOperator
import com.tambapps.marcel.parser.visitor.ExpressionVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

private interface IUnpushedExpressionGenerator: ExpressionVisitor {

  val mv: MethodVisitor

  override fun visit(integer: IntConstantNode) {
    // don't need to write constants
  }

  override fun visit(operator: TernaryNode) {
    TODO("Not yet implemented")
  }

  override fun visit(operator: MulOperator) {
    evaluateOperands(operator)
  }

  override fun visit(operator: DivOperator) {
    evaluateOperands(operator)
  }

  override fun visit(operator: MinusOperator) {
    evaluateOperands(operator)
  }


  override fun visit(operator: PlusOperator) {
    evaluateOperands(operator)
  }

  override fun visit(operator: PowOperator) {
    evaluateOperands(operator)
  }

  private fun evaluateOperands(binaryOperatorNode: BinaryOperatorNode) {
    pushArgument(binaryOperatorNode.leftOperand)
    pushArgument(binaryOperatorNode.rightOperand)
  }

  override fun visit(operator: FunctionCallNode) {
    if (operator.name == "println") {
      mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
    } else {
      throw UnsupportedOperationException("Cannot handle function call yet")
    }
    // TODO add a type field and check types when calling functions
    for (argumentNode in operator.arguments) {
      // write argument on the stack
      pushArgument(argumentNode)
    }
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false)
  }

  fun pushArgument(expr: ExpressionNode)
}

/**
 * Generates expression bytecode but don't push them to the stack. (Useful for statement expressions)
 */
class UnpushedExpressionGenerator(override val mv: MethodVisitor): IUnpushedExpressionGenerator {

  private val expressionGenerator = ExpressionGenerator(mv)
  override fun pushArgument(expr: ExpressionNode) {
    expressionGenerator.pushArgument(expr)
  }

  override fun visit(operator: MulOperator) {
    super.visit(operator)
    drop2()
  }

  override fun visit(operator: DivOperator) {
    super.visit(operator)
    drop2()
  }

  override fun visit(operator: MinusOperator) {
    super.visit(operator)
    drop2()
  }

  override fun visit(operator: PlusOperator) {
    super.visit(operator)
    drop2()
  }

  override fun visit(operator: PowOperator) {
    super.visit(operator)
    drop2()
  }

  fun drop2() {
    TODO("Drop 2 args from the stack")
  }
}

class ExpressionGenerator(override val mv: MethodVisitor): IUnpushedExpressionGenerator {

  override fun visit(integer: IntConstantNode) {
    mv.visitLdcInsn(integer.value) // write primitive value, from an Object class e.g. Integer -> int
  }

  override fun visit(operator: FunctionCallNode) {
    super.visit(operator)
    // TODO push on stack
  }

  override fun visit(operator: TernaryNode) {
    super.visit(operator)
    // TODO push on stack
  }

  override fun pushArgument(expr: ExpressionNode) {
    expr.accept(this)
  }

  override fun visit(operator: MulOperator) {
    super.visit(operator)
    mv.visitInsn(operator.type.mulCode)
  }

  override fun visit(operator: DivOperator) {
    super.visit(operator)
    mv.visitInsn(operator.type.divCode)
  }

  override fun visit(operator: MinusOperator) {
    super.visit(operator)
    mv.visitInsn(operator.type.subCode)
  }


  override fun visit(operator: PlusOperator) {
    super.visit(operator)
    mv.visitInsn(operator.type.addCode)
  }

  override fun visit(operator: PowOperator) {
    super.visit(operator)
    TODO("Implement pow, or call function?")
  }
}