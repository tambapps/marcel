package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.semantic.ast.AstNodeVisitor
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
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
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.VariableVisitor
import com.tambapps.marcel.semantic.variable.field.BoundField
import com.tambapps.marcel.semantic.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.variable.field.JavaClassField
import com.tambapps.marcel.semantic.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.variable.field.MethodField
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MethodWriter(
  private val classNode: ClassNode,
  private val methodNode: MethodNode,
  private val mv: MethodVisitor

): AstNodeVisitor<Unit>, VariableVisitor<Unit> {

  override fun visit(node: ExpressionStatementNode) {
    node.accept(this)
    popStack()
  }

  override fun visit(node: ReturnStatementNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: BlockStatementNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: FunctionCallNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: ReferenceNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: ClassReferenceNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: BoolConstantNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: ByteConstantNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: CharConstantNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: DoubleConstantNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: FloatConstantNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: IntConstantNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: LongConstantNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: NullValueNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: ShortConstantNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: VoidExpressionNode) {
    TODO("Not yet implemented")
  }


  /*
   * Variables
   */

  override fun visit(variable: LocalVariable) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: BoundField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: DynamicMethodField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: JavaClassField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: MarcelArrayLengthField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: MarcelField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: MethodField) {
    TODO("Not yet implemented")
  }


  private fun popStack() {
    mv.visitInsn(Opcodes.POP)
  }

  private fun pop2Stack() {
    mv.visitInsn(Opcodes.POP)
  }
}