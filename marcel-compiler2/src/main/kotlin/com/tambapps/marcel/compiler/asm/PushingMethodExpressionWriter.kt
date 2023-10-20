package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.addCode
import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.divCode
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.modCode
import com.tambapps.marcel.compiler.extensions.mulCode
import com.tambapps.marcel.compiler.extensions.shlCode
import com.tambapps.marcel.compiler.extensions.shrCode
import com.tambapps.marcel.compiler.extensions.subCode
import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.BoolConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.ByteConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.CharConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.DoubleConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.FloatConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.IntConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.LongConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.NullValueNode
import com.tambapps.marcel.semantic.ast.expression.literal.ShortConstantNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.operator.AndNode
import com.tambapps.marcel.semantic.ast.expression.operator.ArrayIndexAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.operator.BinaryArithmeticOperatorNode
import com.tambapps.marcel.semantic.ast.expression.operator.BinaryOperatorNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.GeNode
import com.tambapps.marcel.semantic.ast.expression.operator.GtNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.IsNotEqualNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeNode
import com.tambapps.marcel.semantic.ast.expression.operator.LeftShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.LtNode
import com.tambapps.marcel.semantic.ast.expression.operator.MinusNode
import com.tambapps.marcel.semantic.ast.expression.operator.ModNode
import com.tambapps.marcel.semantic.ast.expression.operator.MulNode
import com.tambapps.marcel.semantic.ast.expression.operator.NotNode
import com.tambapps.marcel.semantic.ast.expression.operator.OrNode
import com.tambapps.marcel.semantic.ast.expression.operator.PlusNode
import com.tambapps.marcel.semantic.ast.expression.operator.RightShiftNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class PushingMethodExpressionWriter(mv: MethodVisitor, typeResolver: JavaTypeResolver, classScopeType: JavaType) :
  MethodExpressionWriter(mv, typeResolver, classScopeType) {

  companion object {
    private val PRIMITIVE_CAST_INSTRUCTION_MAP = mapOf(
      Pair(Pair(JavaType.int, JavaType.long), Opcodes.I2L),
      Pair(Pair(JavaType.int, JavaType.float), Opcodes.I2F),
      Pair(Pair(JavaType.int, JavaType.double), Opcodes.I2D),
      Pair(Pair(JavaType.int, JavaType.boolean), Opcodes.I2B),
      Pair(Pair(JavaType.int, JavaType.char), Opcodes.I2C),
      Pair(Pair(JavaType.long, JavaType.int), Opcodes.L2I),
      Pair(Pair(JavaType.long, JavaType.float), Opcodes.L2F),
      Pair(Pair(JavaType.long, JavaType.double), Opcodes.L2D),
      Pair(Pair(JavaType.float, JavaType.int), Opcodes.F2I),
      Pair(Pair(JavaType.float, JavaType.long), Opcodes.F2L),
      Pair(Pair(JavaType.float, JavaType.double), Opcodes.F2D),
      Pair(Pair(JavaType.double, JavaType.int), Opcodes.D2I),
      Pair(Pair(JavaType.double, JavaType.long), Opcodes.D2L),
      Pair(Pair(JavaType.double, JavaType.float), Opcodes.D2F),
    )

    private val PRIMITIVE_COMPARISON_MAP = mapOf(
      Pair(JavaType.double, Opcodes.DCMPL),
      Pair(JavaType.float, Opcodes.FCMPL),
      Pair(JavaType.long, Opcodes.LCMP),
    )
  }

  override fun visit(node: NotNode) {
    node.expressionNode.accept(this)
    val endLabel = Label()
    val trueLabel = Label()
    mv.visitJumpInsn(Opcodes.IFEQ, trueLabel)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitJumpInsn(Opcodes.GOTO, endLabel)
    mv.visitLabel(trueLabel)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitLabel(endLabel)
  }

  override fun visit(node: VariableAssignmentNode) {
    super.visit(node)
    // push the value on the stack
    node.owner?.accept(this)
    node.variable.accept(loadVariableVisitor)
  }

  override fun visit(node: PlusNode) = arithmeticOperator(node, node.type.addCode)
  override fun visit(node: MinusNode) = arithmeticOperator(node, node.type.subCode)
  override fun visit(node: MulNode) = arithmeticOperator(node, node.type.mulCode)
  override fun visit(node: DivNode) = arithmeticOperator(node, node.type.divCode)
  override fun visit(node: ModNode) = arithmeticOperator(node, node.type.modCode)
  override fun visit(node: LeftShiftNode) = arithmeticOperator(node, node.type.shlCode)
  override fun visit(node: RightShiftNode) = arithmeticOperator(node, node.type.shrCode)

  override fun visit(node: AndNode) {
    val labelFalse = Label()
    val labelEnd = Label()
    node.leftOperand.accept(this)
    mv.visitJumpInsn(Opcodes.IFEQ, labelFalse)
    node.rightOperand.accept(this)
    mv.visitJumpInsn(Opcodes.IFEQ, labelFalse)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitJumpInsn(Opcodes.GOTO, labelEnd)
    mv.visitLabel(labelFalse)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitLabel(labelEnd)
  }

  override fun visit(node: OrNode) {
    val labelTrue = Label()
    val labelFalse = Label()
    val labelEnd = Label()
    node.leftOperand.accept(this)
    mv.visitJumpInsn(Opcodes.IFNE, labelTrue)
    node.rightOperand.accept(this)
    mv.visitJumpInsn(Opcodes.IFEQ, labelFalse)
    mv.visitLabel(labelTrue)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitJumpInsn(Opcodes.GOTO, labelEnd)
    mv.visitLabel(labelFalse)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitLabel(labelEnd)
  }

  override fun visit(node: IsEqualNode) = comparisonOperator(node, Opcodes.IF_ACMPEQ, Opcodes.IF_ICMPEQ)
  override fun visit(node: IsNotEqualNode) = comparisonOperator(node, Opcodes.IF_ACMPNE, Opcodes.IF_ICMPNE)

  // operands should always be primitive for 4 below operators
  override fun visit(node: LtNode) = comparisonOperator(node, -1, Opcodes.IF_ICMPLT)
  override fun visit(node: LeNode) = comparisonOperator(node, -1, Opcodes.IF_ICMPLE)
  override fun visit(node: GtNode) = comparisonOperator(node, -1, Opcodes.IF_ICMPGT)
  override fun visit(node: GeNode) = comparisonOperator(node, -1, Opcodes.IF_ICMPGE)

  private fun comparisonOperator(node: BinaryOperatorNode, objectIfCmpCode: Int, intCmpIfCode: Int) {
    node.leftOperand.accept(this)
    node.rightOperand.accept(this)
    val operandsType = node.leftOperand.type // both operands should have the same type
    val objectComparison = !operandsType.primitive || !node.rightOperand.type.primitive
    if (!objectComparison && operandsType != JavaType.int && operandsType != JavaType.byte && operandsType != JavaType.char) {
      // we need a custom comparison for the types matching this condition
      mv.visitInsn(PRIMITIVE_COMPARISON_MAP[operandsType] ?: throw RuntimeException("Compiler/semantic error."))
      mv.visitLdcInsn(0) // pushing 0 because we're comparing two numbers below
    }

    val endLabel = Label()
    val trueLabel = Label()
    mv.visitJumpInsn(if (objectComparison) objectIfCmpCode else intCmpIfCode, trueLabel)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitJumpInsn(Opcodes.GOTO, endLabel)
    mv.visitLabel(trueLabel)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitLabel(endLabel)
  }

  private fun arithmeticOperator(node: BinaryArithmeticOperatorNode, insCode: Int) {
    node.leftOperand.accept(this)
    node.rightOperand.accept(this)
    mv.visitInsn(insCode)
  }

  override fun visit(node: ClassReferenceNode) {
    if (node.type.primitive) {
      TODO("getField(node, scope, typeResolver.getClassField(clazz.objectType, \"TYPE\", node), true)")
    } else {
      mv.visitLdcInsn(Type.getType(node.type.descriptor))
    }
  }

  override fun visit(node: ThisReferenceNode) = mv.visitVarInsn(Opcodes.ALOAD, 0) // O is this
  // super is actually this. The difference is in the class internalName supplied when performing ASM instructions
  override fun visit(node: SuperReferenceNode) = mv.visitVarInsn(Opcodes.ALOAD, 0)
  override fun visit(node: BoolConstantNode) = mv.visitInsn(if (node.value) Opcodes.ICONST_1 else Opcodes.ICONST_0)

  override fun visit(node: ByteConstantNode) = mv.visitIntInsn(Opcodes.BIPUSH, node.value.toInt())

  override fun visit(node: CharConstantNode) = mv.visitLdcInsn(node.value)

  override fun visit(node: DoubleConstantNode) = mv.visitLdcInsn(node.value)

  override fun visit(node: FloatConstantNode) = mv.visitLdcInsn(node.value)

  override fun visit(node: IntConstantNode) = mv.visitLdcInsn(node.value)

  override fun visit(node: LongConstantNode) = mv.visitLdcInsn(node.value)
  override fun visit(node: StringConstantNode) = mv.visitLdcInsn(node.value)
  override fun visit(node: NullValueNode) = mv.visitInsn(Opcodes.ACONST_NULL)
  override fun visit(node: ShortConstantNode) = mv.visitIntInsn(Opcodes.BIPUSH, node.value.toInt())

  override fun visit(node: ArrayAccessNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: ArrayIndexAssignmentNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: JavaCastNode) {
    node.expressionNode.accept(this)
    val expectedType = node.type
    val actualType = node.expressionNode.type
    if (expectedType.primitive && actualType.primitive) {
      val castInstruction = PRIMITIVE_CAST_INSTRUCTION_MAP[Pair(actualType, expectedType)]
      // might be null because no need to cast for char to int conversion
      if (castInstruction != null) {
        mv.visitInsn(castInstruction)
      }
    } else {
      mv.visitTypeInsn(Opcodes.CHECKCAST, expectedType.internalName)
    }
  }

  override fun visit(node: InstanceOfNode) {
    node.expressionNode.accept(this)
    mv.visitTypeInsn(Opcodes.INSTANCEOF, node.instanceType.internalName)
  }

  override fun pushExpression(node: ExpressionNode) = node.accept(this)
}