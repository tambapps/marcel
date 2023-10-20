package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.returnCode
import com.tambapps.marcel.compiler.extensions.visitMethodInsn
import com.tambapps.marcel.semantic.ast.expression.ArrayAccessNode
import com.tambapps.marcel.semantic.ast.expression.ClassReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.InstanceOfNode
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.NewInstanceNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.StringNode
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
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.expression.operator.VariableAssignmentNode
import com.tambapps.marcel.semantic.ast.expression.literal.ArrayNode
import com.tambapps.marcel.semantic.ast.expression.literal.MapNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.expression.operator.AndNode
import com.tambapps.marcel.semantic.ast.expression.operator.ArrayIndexAssignmentNode
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
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MethodInstructionWriter(
  mv: MethodVisitor, typeResolver: JavaTypeResolver, classScopeType: JavaType
): MethodExpressionWriter(mv, typeResolver, classScopeType), StatementNodeVisitor<Unit> {

  private val expressionPusher = PushingMethodExpressionWriter(mv, typeResolver, classScopeType)

  /*
   * Statements
   */
  override fun visit(node: ExpressionStatementNode) = node.expressionNode.accept(this)

  override fun visit(node: ReturnStatementNode) {
    pushExpression(node.expressionNode)
    mv.visitInsn(node.expressionNode.type.returnCode)
  }

  override fun visit(node: BlockStatementNode) {
    node.statements.forEach {
      it.accept(this)
    }
  }

  override fun visit(node: IfStatementNode) {
    pushExpression(node.conditionNode)
    val endLabel = Label()
    val falseStatementNode = node.falseStatementNode
    if (falseStatementNode == null) {
      mv.visitJumpInsn(Opcodes.IFEQ, endLabel)
      node.trueStatementNode.accept(this)
      mv.visitLabel(endLabel)
    } else {
      val falseLabel = Label()
      mv.visitJumpInsn(Opcodes.IFEQ, falseLabel)
      node.trueStatementNode.accept(this)
      mv.visitJumpInsn(Opcodes.GOTO, endLabel)
      mv.visitLabel(falseLabel)
      falseStatementNode.accept(this)
      mv.visitLabel(endLabel)
    }
  }

  override fun visit(node: ForInIteratorStatementNode) {
    // assign the iterator to a variable
    visit(VariableAssignmentNode(node.iteratorVariable, node.iteratorExpression, node.tokenStart, node.tokenEnd))

    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition -> iterator.hasNext()
    val iteratorVarReference = ReferenceNode(owner = null, node.iteratorVariable, node.token)
    pushExpression(iteratorVarReference)
    mv.visitMethodInsn(typeResolver.findMethodOrThrow(Iterator::class.javaType, "hasNext", emptyList()))

    val loopEnd = Label()
    mv.visitJumpInsn(Opcodes.IFEQ, loopEnd)

    // loop body
    visit(VariableAssignmentNode(node.variable, node.nextMethodCall, node.tokenStart, node.tokenEnd))
    node.bodyStatement.accept(this)
    mv.visitJumpInsn(Opcodes.GOTO, loopStart)

    // loop end
    mv.visitLabel(loopEnd)
  }

  /*
   * Expressions
   */
  override fun visit(node: AndNode) {
    val labelFalse = Label()
    node.leftOperand.accept(this)
    mv.visitJumpInsn(Opcodes.IFEQ, labelFalse)
    node.rightOperand.accept(this)
    mv.visitLabel(labelFalse)
  }

  override fun visit(node: OrNode) {
    val labelTrue = Label()
    node.leftOperand.accept(this)
    mv.visitJumpInsn(Opcodes.IFNE, labelTrue)
    node.rightOperand.accept(this)
    mv.visitLabel(labelTrue)
  }

  override fun visit(node: NotNode) {
    node.expressionNode.accept(this)
  }

  override fun visit(node: ArrayIndexAssignmentNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: ArrayAccessNode) {
    TODO("Not yet implemented")
  }

  override fun visit(node: LeftShiftNode) = binaryOperator(node)

  override fun visit(node: RightShiftNode) = binaryOperator(node)

  override fun visit(node: DivNode) = binaryOperator(node)

  override fun visit(node: MinusNode) = binaryOperator(node)

  override fun visit(node: ModNode) = binaryOperator(node)

  override fun visit(node: MulNode) = binaryOperator(node)

  override fun visit(node: PlusNode) = binaryOperator(node)

  override fun visit(node: IsEqualNode) = binaryOperator(node)

  override fun visit(node: IsNotEqualNode) = binaryOperator(node)

  override fun visit(node: GeNode) = binaryOperator(node)

  override fun visit(node: GtNode) = binaryOperator(node)

  override fun visit(node: LeNode) = binaryOperator(node)

  override fun visit(node: LtNode) = binaryOperator(node)

  override fun visit(node: FunctionCallNode) {
    super.visit(node)
    popStackIfNotVoid(node.javaMethod.returnType)
  }

  override fun visit(node: NewInstanceNode) {
    super.visit(node)
    popStackIfNotVoid(node.type)
  }

  override fun visit(node: ReferenceNode) {
    super.visit(node)
    popStackIfNotVoid(node.type)
  }

  override fun visit(node: ClassReferenceNode) {}

  override fun visit(node: ThisReferenceNode) {}

  override fun visit(node: SuperReferenceNode) {}

  override fun visit(node: JavaCastNode) {
    node.expressionNode.accept(this)
  }

  override fun visit(node: InstanceOfNode) {
    node.expressionNode.accept(this)
  }

  override fun visit(node: BoolConstantNode) {}

  override fun visit(node: ByteConstantNode) {}

  override fun visit(node: CharConstantNode) {}

  override fun visit(node: StringConstantNode) {}

  override fun visit(node: DoubleConstantNode) {}

  override fun visit(node: FloatConstantNode) {}

  override fun visit(node: IntConstantNode) {}

  override fun visit(node: LongConstantNode) {}

  override fun visit(node: NullValueNode) {}

  override fun visit(node: ShortConstantNode) {}

  override fun visit(node: StringNode) {
    super.visit(node)
    popStackIfNotVoid(node.type)
  }

  override fun visit(node: ArrayNode) {
    super.visit(node)
    popStackIfNotVoid(node.type)
  }

  override fun visit(node: MapNode) {
    super.visit(node)
    popStackIfNotVoid(node.type)
  }

  private fun binaryOperator(node: BinaryOperatorNode) {
    node.leftOperand.accept(this)
    node.rightOperand.accept(this)
  }

  override fun pushExpression(node: ExpressionNode) = node.accept(expressionPusher)

}