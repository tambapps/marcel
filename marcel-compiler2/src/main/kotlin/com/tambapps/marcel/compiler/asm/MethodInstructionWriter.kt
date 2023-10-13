package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.invokeCode
import com.tambapps.marcel.compiler.extensions.returnCode
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.semantic.ast.expression.SuperReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisConstructorCallNode
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
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaType.Companion.boolean
import com.tambapps.marcel.semantic.type.JavaType.Companion.char
import com.tambapps.marcel.semantic.type.JavaType.Companion.double
import com.tambapps.marcel.semantic.type.JavaType.Companion.float
import com.tambapps.marcel.semantic.type.JavaType.Companion.int
import com.tambapps.marcel.semantic.type.JavaType.Companion.long
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class MethodInstructionWriter(
  private val mv: MethodVisitor,
  private val classScopeType: JavaType
): AstNodeVisitor<Unit> {

  companion object {
    private val PRIMITIVE_CAST_INSTRUCTION_MAP = mapOf(
      Pair(Pair(int, long), Opcodes.I2L),
      Pair(Pair(int, float), Opcodes.I2F),
      Pair(Pair(int, double), Opcodes.I2D),
      Pair(Pair(int, boolean), Opcodes.I2B),
      Pair(Pair(int, char), Opcodes.I2C),
      Pair(Pair(long, int), Opcodes.L2I),
      Pair(Pair(long, float), Opcodes.L2F),
      Pair(Pair(long, double), Opcodes.L2D),
      Pair(Pair(float, int), Opcodes.F2I),
      Pair(Pair(float, long), Opcodes.F2L),
      Pair(Pair(float, double), Opcodes.F2D),
      Pair(Pair(double, int), Opcodes.D2I),
      Pair(Pair(double, long), Opcodes.D2L),
      Pair(Pair(double, float), Opcodes.D2F),
    )
  }

  private val loadVariableVisitor = LoadVariableVisitor(mv, classScopeType)
  private val storeVariableVisitor = StoreVariableVisitor(mv, classScopeType)

  override fun visit(node: ExpressionStatementNode) {
    node.expressionNode.accept(this)
    if (node.expressionNode.type != JavaType.void) popStack(node.expressionNode.type)
  }

  override fun visit(node: ReturnStatementNode) {
    node.expressionNode.accept(this)
    // TODO cast if necessary. BUT MarcelSemantic should throw an error (if any)
    mv.visitInsn(node.expressionNode.type.returnCode)
  }

  override fun visit(node: BlockStatementNode) {
    node.statements.forEach {
      it.accept(this)
    }
  }

  override fun visit(node: VariableAssignmentNode) {
    node.expression.accept(this)
    node.variable.accept(storeVariableVisitor)
    // push the value on the stack
    node.variable.accept(loadVariableVisitor)
  }

  override fun visit(node: FunctionCallNode) {
    node.owner?.accept(this)
    for (argumentNode in node.arguments) {
      argumentNode.accept(this)
    }
    mv.visitMethodInsn(node.javaMethod.invokeCode, node.javaMethod.ownerClass.internalName, node.javaMethod.name, node.javaMethod.descriptor, node.javaMethod.ownerClass.isInterface)
  }

  override fun visit(node: ThisConstructorCallNode) {
    visitOwnConstructorCall(node.classType, node.javaMethod, node.arguments)
  }
  override fun visit(node: SuperConstructorCallNode) {
    visitOwnConstructorCall(node.classType, node.javaMethod, node.arguments)
  }

  private fun visitOwnConstructorCall(type: JavaType, method: JavaMethod, arguments: List<ExpressionNode>) {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    arguments.forEach { it.accept(this) }
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, type.internalName, JavaMethod.CONSTRUCTOR_NAME,
      // void return type for constructors
      method.descriptor, false)

  }
  override fun visit(node: ReferenceNode) = node.variable.accept(loadVariableVisitor)

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

  override fun visit(node: NullValueNode) = mv.visitInsn(Opcodes.ACONST_NULL)

  override fun visit(node: ShortConstantNode) = mv.visitIntInsn(Opcodes.BIPUSH, node.value.toInt())

  override fun visit(node: VoidExpressionNode) {
    // push nothing
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

  // TODO popping might depend on type as long and double takes 2 slots
  private fun popStack(type: JavaType) {
    mv.visitInsn(if (type == long || type == double) Opcodes.POP2 else Opcodes.POP)
  }

}