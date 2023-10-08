package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.invokeCode
import com.tambapps.marcel.compiler.extensions.returnCode
import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
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
import com.tambapps.marcel.semantic.type.JavaType
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class MethodInstructionWriter(
  private val mv: MethodVisitor
): AstNodeVisitor<Unit> {

  private val loadVariableVisitor = LoadVariableVisitor(mv)

  override fun visit(node: ExpressionStatementNode) {
    node.expressionNode.accept(this)
    if (node.expressionNode.type != JavaType.void) popStack()
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

  override fun visit(node: FunctionCallNode) {
    node.owner?.accept(this)
    for (argumentNode in node.arguments) {
      argumentNode.accept(this)
    }
    mv.visitMethodInsn(node.javaMethod.invokeCode, node.javaMethod.ownerClass.internalName, node.javaMethod.name, node.javaMethod.descriptor, node.javaMethod.ownerClass.isInterface)
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

  private fun popStack() {
    mv.visitInsn(Opcodes.POP)
  }

  private fun pop2Stack() {
    mv.visitInsn(Opcodes.POP2)
  }
}