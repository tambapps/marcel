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

  override fun visit(ternaryNode: TernaryNode) {
    TODO("Not yet implemented")
  }

  override fun visit(mulOperator: MulOperator) {
    evaluateOperands(mulOperator)
    onBinaryOperatorEnd(mulOperator)
  }

  override fun visit(divOperator: DivOperator) {
    evaluateOperands(divOperator)
    onBinaryOperatorEnd(divOperator)
  }

  override fun visit(minusOperator: MinusOperator) {
    evaluateOperands(minusOperator)
    onBinaryOperatorEnd(minusOperator)
  }


  override fun visit(plusOperator: PlusOperator) {
    evaluateOperands(plusOperator)
    onBinaryOperatorEnd(plusOperator)
  }

  override fun visit(powOperator: PowOperator) {
    evaluateOperands(powOperator)
    onBinaryOperatorEnd(powOperator)
  }

  private fun evaluateOperands(binaryOperatorNode: BinaryOperatorNode) {
    pushArgument(binaryOperatorNode.leftOperand)
    pushArgument(binaryOperatorNode.rightOperand)
  }

  fun onBinaryOperatorEnd(binaryOperatorNode: BinaryOperatorNode)

  override fun visit(functionCallNode: FunctionCallNode) {
    if (functionCallNode.name == "println") {
      mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
    } else {
      throw UnsupportedOperationException("Cannot handle function call yet")
    }
    // TODO add a type field and check types when calling functions
    for (argumentNode in functionCallNode.arguments) {
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

  override fun onBinaryOperatorEnd(binaryOperatorNode: BinaryOperatorNode) {
    TODO("Not yet implemented")
  }
}

class ExpressionGenerator(override val mv: MethodVisitor): IUnpushedExpressionGenerator {

  override fun visit(integer: IntConstantNode) {
    mv.visitLdcInsn(integer.value) // write primitive value, from an Object class e.g. Integer -> int
  }

  override fun visit(mulOperator: MulOperator) {
    super.visit(mulOperator)
    // TODO push on stack
  }

  override fun visit(functionCallNode: FunctionCallNode) {
    super.visit(functionCallNode)
    // TODO push on stack
  }

  override fun visit(ternaryNode: TernaryNode) {
    super.visit(ternaryNode)
    // TODO push on stack
  }

  override fun pushArgument(expr: ExpressionNode) {
    expr.accept(this)
  }

  override fun onBinaryOperatorEnd(binaryOperatorNode: BinaryOperatorNode) {
    // TODO opcodes is specific per primitive type AND operation
    mv.visitInsn(Opcodes.IADD)

  }
}