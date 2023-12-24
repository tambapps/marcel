package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.addCode
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.returnCode
import com.tambapps.marcel.compiler.extensions.visitMethodInsn
import com.tambapps.marcel.semantic.ast.AstNode
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
import com.tambapps.marcel.semantic.ast.expression.operator.BinaryOperatorNode
import com.tambapps.marcel.semantic.ast.expression.operator.DivNode
import com.tambapps.marcel.semantic.ast.expression.operator.ElvisNode
import com.tambapps.marcel.semantic.ast.expression.operator.GeNode
import com.tambapps.marcel.semantic.ast.expression.operator.GtNode
import com.tambapps.marcel.semantic.ast.expression.operator.IncrNode
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
import com.tambapps.marcel.semantic.ast.statement.BreakNode
import com.tambapps.marcel.semantic.ast.statement.CatchNode
import com.tambapps.marcel.semantic.ast.statement.ContinueNode
import com.tambapps.marcel.semantic.ast.statement.ForInIteratorStatementNode
import com.tambapps.marcel.semantic.ast.statement.ForStatementNode
import com.tambapps.marcel.semantic.ast.statement.IfStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNodeVisitor
import com.tambapps.marcel.semantic.ast.statement.ThrowNode
import com.tambapps.marcel.semantic.ast.statement.TryCatchNode
import com.tambapps.marcel.semantic.ast.statement.WhileNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.variable.LocalVariable
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.util.LinkedList

class MethodInstructionWriter(
  mv: MethodVisitor, typeResolver: JavaTypeResolver, classScopeType: JavaType
): MethodExpressionWriter(mv, typeResolver, classScopeType), StatementNodeVisitor<Unit> {

  private val expressionPusher = PushingMethodExpressionWriter(mv, typeResolver, classScopeType)

  private val loopContextQueue = LinkedList<LoopContext>()
  private val currentLoopContext get() = loopContextQueue.peek()

  /*
   * Statements
   */
  override fun visit(node: ExpressionStatementNode) {
    label(node)
    node.expressionNode.accept(this)
  }

  override fun visit(node: ReturnStatementNode) {
    label(node.expressionNode)
    pushExpression(node.expressionNode)
    mv.visitInsn(node.expressionNode.type.returnCode)
  }

  override fun visit(node: BlockStatementNode) {
    label(node)
    node.statements.forEach {
      it.accept(this)
    }
  }

  override fun visit(node: IfStatementNode) {
    label(node.conditionNode)
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

  override fun visit(node: ElvisNode) {
    label(node.leftOperand)
    pushExpression(node.leftOperand)
    // at this point we have on the stack
    // - expression
    // - truthyCastedExpression

    val endLabel = Label()
    mv.visitJumpInsn(Opcodes.IFNE, endLabel)
    label(node.rightOperand)
    node.rightOperand.accept(this)
    mv.visitLabel(endLabel)

    // we consumed the truthyCastedExpression but expression is still on the stack
    // so we need to pop it
    popStackIfNotVoid(node.type)
  }

  override fun visit(node: ForInIteratorStatementNode) {
    label(node.iteratorExpression)
    // assign the iterator to a variable
    visit(VariableAssignmentNode(node.iteratorVariable, node.iteratorExpression, node.tokenStart, node.tokenEnd))

    val loopStart = Label()
    val loopEnd = Label()
    loopContextQueue.push(LoopContext(continueLabel = loopStart, breakLabel = loopEnd))
    // loop start
    mv.visitLabel(loopStart)

    // Verifying condition -> iterator.hasNext()
    val iteratorVarReference = ReferenceNode(variable = node.iteratorVariable, token = node.token)
    pushExpression(iteratorVarReference)
    mv.visitMethodInsn(typeResolver.findMethodOrThrow(Iterator::class.javaType, "hasNext", emptyList()))

    mv.visitJumpInsn(Opcodes.IFEQ, loopEnd)

    // loop body
    label(node.nextMethodCall)
    visit(VariableAssignmentNode(node.variable, node.nextMethodCall, node.tokenStart, node.tokenEnd))
    node.bodyStatement.accept(this)
    mv.visitJumpInsn(Opcodes.GOTO, loopStart)

    // loop end
    mv.visitLabel(loopEnd)
    loopContextQueue.pop()
  }

  override fun visit(node: WhileNode) {
    // loop start
    val loopStart = Label()
    mv.visitLineNumber(node.condition.token.line + 1, loopStart)
    val loopEnd = Label()
    loopContextQueue.push(LoopContext(continueLabel = loopStart, breakLabel = loopEnd))
    mv.visitLabel(loopStart)

    // Verifying condition
    pushExpression(node.condition)
    mv.visitJumpInsn(Opcodes.IFEQ, loopEnd)

    // loop body
    node.statement.accept(this)

    // Return to the beginning of the loop
    mv.visitJumpInsn(Opcodes.GOTO, loopStart)

    // loop end
    mv.visitLabel(loopEnd)
    loopContextQueue.pop()
  }

  override fun visit(node: ForStatementNode) {
    label(node.initStatement)
    // initialization
    node.initStatement.accept(this)

    val incrementLabel = Label()
    val loopEnd = Label()
    loopContextQueue.push(LoopContext(continueLabel = incrementLabel, breakLabel = loopEnd))
    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)
    mv.visitLineNumber(node.condition.token.line + 1, loopStart)

    // Verifying condition
    pushExpression(node.condition)
    mv.visitJumpInsn(Opcodes.IFEQ, loopEnd)

    // loop body
    node.bodyStatement.accept(this)

    // iteration
    mv.visitLabel(incrementLabel)
    node.iteratorStatement.accept(this)
    mv.visitJumpInsn(Opcodes.GOTO, loopStart)

    // loop end
    mv.visitLabel(loopEnd)
    loopContextQueue.pop()
  }

  override fun visit(node: ThrowNode) {
    label(node.expressionNode)
    pushExpression(node.expressionNode)
    mv.visitInsn(Opcodes.ATHROW)
  }

  override fun visit(node: BreakNode) {
    label(node)
    mv.visitJumpInsn(Opcodes.GOTO, currentLoopContext.breakLabel)
  }

  override fun visit(node: ContinueNode) {
    label(node)
    mv.visitJumpInsn(Opcodes.GOTO, currentLoopContext.continueLabel)
  }

  override fun visit(node: TryCatchNode) {
    label(node)
    if (node.finallyNode == null) {
      tryCatch(node)
    } else if (node.catchNodes.isEmpty()) {
      tryFinally(node)
    } else {
      tryCatchFinally(node)
    }
  }

  private fun tryCatchFinally(node: TryCatchNode) {
    // TODO bytecode looks weird but it seems to work
    val tryStart = Label()
    val tryEnd = Label()
    val finallyLabel = Label()
    val endLabel = Label()

    val catchNodes = node.catchNodes
    val catchLabelMap = generateCatchLabel(catchNodes, tryStart, tryEnd)
    val finallyNode = node.finallyNode!!

    mv.visitTryCatchBlock(tryStart, tryEnd, finallyLabel, null)

    tryBranch(node, tryStart, tryEnd, endLabel)

    catchNodes.forEach { catchNode ->
      catchBlock(catchNode.throwableVariable, catchLabelMap.getValue(catchNode))
      catchNode.statement.accept(this)

      finallyNode.statement.accept(this)

      mv.visitJumpInsn(Opcodes.GOTO, endLabel)
    }

    handleFinally(node.finallyNode!!, finallyLabel, endLabel)
  }

  private fun tryCatch(node: TryCatchNode) {
    val tryStart = Label()
    val tryEnd = Label()
    val endLabel = Label()
    val catchNodes = node.catchNodes
    val catchLabelMap = generateCatchLabel(catchNodes, tryStart, tryEnd)

    tryBranch(node, tryStart, tryEnd, endLabel)

    catchNodes.forEach { catchNode ->
      catchBlock(catchNode.throwableVariable, catchLabelMap.getValue(catchNode))
      catchNode.statement.accept(this)
      mv.visitJumpInsn(Opcodes.GOTO, endLabel)
    }

    mv.visitLabel(endLabel)
  }

  private fun tryFinally(node: TryCatchNode) {
    val tryStart = Label()
    val tryEnd = Label()
    val finallyLabel = Label()
    val endLabel = Label()

    mv.visitTryCatchBlock(tryStart, tryEnd, finallyLabel, null)

    tryBranch(node, tryStart, tryEnd, endLabel)

    handleFinally(node.finallyNode!!, finallyLabel, endLabel)
  }

  private fun handleFinally(finallyNode: CatchNode, finallyLabel: Label, endLabel: Label) {
    val throwableVar = finallyNode.throwableVariable
    catchBlock(throwableVar, finallyLabel)
    finallyNode.statement.accept(this)
    throwableVar.accept(loadVariableVisitor)
    mv.visitInsn(Opcodes.ATHROW)
    mv.visitJumpInsn(Opcodes.GOTO, endLabel)

    mv.visitLabel(endLabel)
  }

  private fun tryBranch(node: TryCatchNode, tryStart: Label, tryEnd: Label, endLabel: Label) {
    mv.visitLabel(tryStart)
    node.tryStatementNode.accept(this)
    mv.visitLabel(tryEnd)
    mv.visitJumpInsn(Opcodes.GOTO, endLabel)
  }

  private fun catchBlock(throwableVariable: LocalVariable, label: Label) {
    mv.visitLabel(label)
    mv.visitVarInsn(Opcodes.ASTORE, throwableVariable.index)
  }

  private fun generateCatchLabel(catchNodes: List<CatchNode>, tryStart: Label, tryEnd: Label): Map<CatchNode, Label> {
    val map: Map<CatchNode, Label> = catchNodes.associateBy(keySelector = { it }, valueTransform = { Label() })
    map.forEach { (node, label) ->
      node.throwableTypes.forEach { throwableType ->
        mv.visitTryCatchBlock(tryStart, tryEnd, label, throwableType.internalName)
      }
    }
    return map
  }

  /**
   * Add line number to bytecode, so that it can be displayed when an exception occured.
   * Only useful for statements
   */
  private fun label(node: AstNode) = Label().apply {
    mv.visitLabel(this)
    mv.visitLineNumber(node.token.line + 1, this)
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

  override fun visit(node: IncrNode) {
    val variable = node.variable
    if (variable is LocalVariable && variable.type == JavaType.int) {
      mv.visitIincInsn(variable.index, node.amount as Int)
      return
    }
    if (node.owner != null) {
      pushExpression(node.owner!!)
      mv.visitInsn(Opcodes.DUP)
    }
    variable.accept(loadVariableVisitor)
    pushConstant(node.amount)
    mv.visitInsn(node.primitiveType.addCode)
    variable.accept(storeVariableVisitor)
  }


  override fun visit(node: ArrayAccessNode) {
    node.owner.accept(this)
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
  override fun pushConstant(value: Any) = expressionPusher.pushConstant(value)

}