package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.AccessOperator
import com.tambapps.marcel.parser.ast.expression.BinaryOperatorNode
import com.tambapps.marcel.parser.ast.expression.BlockNode
import com.tambapps.marcel.parser.ast.expression.BooleanConstantNode
import com.tambapps.marcel.parser.ast.expression.BooleanExpressionNode
import com.tambapps.marcel.parser.ast.expression.ComparisonOperatorNode
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.DivOperator
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.IncrNode
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.MinusOperator
import com.tambapps.marcel.parser.ast.expression.MulOperator
import com.tambapps.marcel.parser.ast.expression.NotNode
import com.tambapps.marcel.parser.ast.expression.NullValueNode
import com.tambapps.marcel.parser.ast.expression.PlusOperator
import com.tambapps.marcel.parser.ast.expression.PowOperator
import com.tambapps.marcel.parser.ast.expression.RangeNode
import com.tambapps.marcel.parser.ast.expression.ReturnNode
import com.tambapps.marcel.parser.ast.expression.StringConstantNode
import com.tambapps.marcel.parser.ast.expression.StringNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.TernaryNode
import com.tambapps.marcel.parser.ast.expression.ToStringNode
import com.tambapps.marcel.parser.ast.expression.UnaryMinus
import com.tambapps.marcel.parser.ast.expression.UnaryPlus
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.ast.expression.VariableReferenceExpression
import com.tambapps.marcel.parser.ast.expression.VoidExpression
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import it.unimi.dsi.fastutil.ints.IntIterator
import marcel.lang.IntRange
import marcel.lang.IntRanges
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes

// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.if_icmp_cond
// https://asm.ow2.io/asm4-guide.pdf
// https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions
private interface IInstructionGenerator: AstNodeVisitor {

  val mv: MethodBytecodeVisitor


  //TODO don't forget to push or not these TODOs() once done, based on the IUnpushedExpressionGenerator implementation
  override fun visit(unaryMinus: UnaryMinus) {
   visit(MinusOperator(IntConstantNode(0), unaryMinus.operand))
  }

  override fun visit(unaryPlus: UnaryPlus) {
    unaryPlus.operand.accept(this)
  }

  override fun visit(operator: TernaryNode) {
    TODO("Not yet implemented")
  }


  override fun visit(fCall: ConstructorCallNode) {
    if (fCall.type.primitive) {
      throw SemanticException("Cannot instantiate a primitive type")
    }
    mv.visitConstructorCall(fCall) {
      pushFunctionCallArguments(fCall)
    }
  }

  override fun visit(fCall: SuperConstructorCallNode) {
    mv.visitSuperConstructorCall(fCall) {
      pushFunctionCallArguments(fCall)
    }
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

  override fun visit(comparisonOperator: ComparisonOperatorNode) {
    if (!comparisonOperator.leftOperand.type.primitive || !comparisonOperator.rightOperand.type.primitive) {
      TODO("Doesn't handle comparison for non primitive types for now")
    }
    evaluateOperands(comparisonOperator)
  }

  override fun visit(accessOperator: AccessOperator) {
    val access = accessOperator.rightOperand
    if (access is FunctionCallNode) {
      access.accept(this)
    } else {
      throw UnsupportedOperationException("Cannot handle such access")
    }
  }

  private fun evaluateOperands(binaryOperatorNode: BinaryOperatorNode) {
    pushArgument(binaryOperatorNode.leftOperand)
    pushArgument(binaryOperatorNode.rightOperand)
  }

  override fun visit(fCall: FunctionCallNode) {
    if (fCall.name == "println") { // TODO big hack for println. Maybe just call a static function from Marcel stdlib
      mv.visitPrintlnCall(fCall) {
        for (argumentNode in fCall.arguments) {
          // write argument on the stack
          pushArgument(argumentNode)
        }
      }
    } else {
      val method = fCall.method
      val methodOwner = fCall.methodOwnerType
      if (!method.isStatic) {
        if (methodOwner is ExpressionNode) {
          pushArgument(methodOwner) // for instance method, we need to push owner
        } else {
          pushArgument(VariableReferenceExpression(fCall.scope, "this"))
        }
      }
      pushFunctionCallArguments(fCall)
      mv.invokeMethod(method)
    }
  }

  private fun pushFunctionCallArguments(fCall: FunctionCallNode) {
    val method = fCall.method
    if (method.parameters.size != fCall.arguments.size) {
      throw SemanticException("Tried to call function $method with ${fCall.arguments.size} instead of ${method.parameters.size}")
    }
    for (argument in fCall.arguments) {
      pushArgument(argument)
    }
  }
  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    pushArgument(variableAssignmentNode.expression)
    mv.visitVariableAssignment(variableAssignmentNode)
  }

  override fun visit(voidExpression: VoidExpression) {
    // do nothing, it's void
  }
  fun pushArgument(expr: ExpressionNode)
}

/**
 * Generates expression bytecode but don't push them to the stack. (Useful for statement expressions)
 */
class InstructionGenerator(override val mv: MethodBytecodeVisitor): IInstructionGenerator {

  private val pushingInstructionGenerator = PushingInstructionGenerator(mv)

  init {
    pushingInstructionGenerator.instructionGenerator = this
  }

  override fun visit(whileStatement: WhileStatement) {
    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition
    whileStatement.condition.accept(pushingInstructionGenerator)
    val loopEnd = Label()
    mv.jumpIfEq(loopEnd)

    // loop body
    loopBody(whileStatement.body, loopStart, loopEnd)

    // Return to the beginning of the loop
    mv.jumpTo(loopStart)

    // loop end
    mv.visitLabel(loopEnd)
  }
  override fun visit(forStatement: ForStatement) {
    // initialization
    forStatement.initStatement.accept(this)

   // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition
    forStatement.endCondition.accept(pushingInstructionGenerator)
    val loopEnd = Label()
    mv.jumpIfEq(loopEnd)

    // loop body
    val incrementLabel = Label()
    loopBody(forStatement.body, incrementLabel, loopEnd)

    // iteration
    mv.visitLabel(incrementLabel)
    forStatement.iteratorStatement.accept(this)
    mv.jumpTo(loopStart)

    // loop end
    mv.visitLabel(loopEnd)
  }

  override fun visit(forInStatement: ForInStatement) {
    val expression = forInStatement.inExpression
    if (!JavaType(Iterable::class.java).isAssignableFrom(expression.type)) {
      throw SemanticException("Only support for in of ranges for now")
    }
    // initialization
    val body = forInStatement.body
    val scope = body.scope
    scope.addLocalVariable(forInStatement.variableType, forInStatement.variableName)

    // creating iterator
    val iteratorVarName = "_tempIterator"
    val getIteratorMethod = scope.getMethodForType(expression.type, "iterator", emptyList())
    val methodName = if (JavaType(IntIterator::class.java).isAssignableFrom(getIteratorMethod.returnType)) "nextInt"
    else if (JavaType(IntIterator::class.java).isAssignableFrom(getIteratorMethod.returnType)) "next"
    else throw UnsupportedOperationException("wtf")
    visit(VariableDeclarationNode(scope, getIteratorMethod.returnType, iteratorVarName,
      FunctionCallNode(scope, "iterator", mutableListOf(), expression)))

    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition
    val iteratorVarReference = VariableReferenceExpression(scope, iteratorVarName)
    pushArgument(iteratorVarReference)
    mv.invokeMethod(IntIterator::class.java.getMethod("hasNext"))

    val loopEnd = Label()
    mv.jumpIfEq(loopEnd)

    // loop body
    visit(VariableAssignmentNode(scope, forInStatement.variableName, FunctionCallNode(scope, methodName, mutableListOf(), iteratorVarReference)))
    loopBody(ExpressionStatementNode(forInStatement.body), loopStart, loopEnd)
    mv.jumpTo(loopStart)

    // loop end
    mv.visitLabel(loopEnd)
  }
  private fun loopBody(body: StatementNode, continueLabel: Label, breakLabel: Label) {
    if (body is ExpressionStatementNode && body.expression is BlockNode && (body.expression as BlockNode).scope is InnerScope) {
      val scope = (body.expression as BlockNode).scope as InnerScope
      scope.continueLabel = continueLabel
      scope.breakLabel = breakLabel
    }
    body.accept(this)
  }
  override fun visit(breakLoopNode: BreakLoopNode) {
    val label = breakLoopNode.scope.breakLabel ?: throw SemanticException("Cannot use break statement outside of a loop")
    mv.jumpTo(label)
  }

  override fun visit(continueLoopNode: ContinueLoopNode) {
    val label = continueLoopNode.scope.continueLabel ?: throw SemanticException("Cannot use break statement outside of a loop")
    mv.jumpTo(label)
  }

  override fun visit(ifStatementNode: IfStatementNode) {
    ifStatementNode.condition.accept(pushingInstructionGenerator)
    val endLabel = Label()
    if (ifStatementNode.falseStatementNode == null) {
      mv.jumpIfEq(endLabel)
      ifStatementNode.trueStatementNode.accept(this)
      mv.visitLabel(endLabel)
    } else {
      val falseStatementNode = ifStatementNode.falseStatementNode!!
      val falseLabel = Label()
      mv.jumpIfEq(falseLabel)
      ifStatementNode.trueStatementNode.accept(this)
      mv.jumpTo(endLabel)
      mv.visitLabel(falseLabel)
      falseStatementNode.accept(this)
      mv.visitLabel(endLabel)
    }
  }
  override fun visit(integer: IntConstantNode) {
    // don't need to write constants
  }

  override fun visit(booleanConstantNode: BooleanConstantNode) {
    // don't need to write constants
  }
  override fun visit(stringConstantNode: StringConstantNode) {
    // don't need to write constants
  }

  override fun visit(fCall: ConstructorCallNode) {
    super.visit(fCall)
    mv.popStack() // don't really know if it's necessary
  }
  override fun visit(fCall: FunctionCallNode) {
    super.visit(fCall)
    if (fCall.type != JavaType.void) {
      mv.popStack() // don't really know if it's necessary
    }
  }
  override fun visit(toStringNode: ToStringNode) {
    toStringNode.expressionNode.accept(this)
  }
  override fun visit(stringNode: StringNode) {
    for (part in stringNode.parts) {
      part.accept(this)
    }
  }

  override fun visit(rangeNode: RangeNode) {
    pushingInstructionGenerator.visit(rangeNode)
    mv.popStack()
  }

  override fun visit(booleanExpression: BooleanExpressionNode) {
    booleanExpression.innerExpression.accept(this)
  }

  override fun visit(incrNode: IncrNode) {
    if (incrNode.variableReference.type == JavaType.int) {
      mv.incr(incrNode)
    } else {
      TODO("Don't support other types than int for increment")
    }
  }

  override fun visit(nullValueNode: NullValueNode) {
    // no need to push anything
  }

  override fun visit(variableReferenceExpression: VariableReferenceExpression) {
    // don't need to push value to the stack by default
  }
  override fun pushArgument(expr: ExpressionNode) {
    pushingInstructionGenerator.pushArgument(expr)
  }


  override fun visit(blockNode: FunctionBlockNode) {
    for (i in 0..(blockNode.statements.size - 2)) {
      blockNode.statements[i].accept(this)
    }
    val lastStatement = blockNode.statements.lastOrNull() ?: ExpressionStatementNode(VoidExpression())
    if (blockNode.scope.returnType == JavaType.void) {
      lastStatement.accept(this)
      mv.returnVoid()
    } else {
      if (!blockNode.scope.returnType.isAssignableFrom(lastStatement.type)) {
        throw SemanticException("Expected return type ${blockNode.scope.returnType} but got ${lastStatement.type}")
      }
      if (lastStatement.type != JavaType.void) {
        pushArgument(lastStatement.expression)
      } else {
        lastStatement.accept(this)
        // method expects an object but nothing was returned? let's return null
        mv.pushNull()
      }
      mv.returnCode(blockNode.scope.returnType.returnCode)
    }
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

  override fun visit(comparisonOperatorNode: ComparisonOperatorNode) {
    super.visit(comparisonOperatorNode)
    drop2()
  }

  override fun visit(notNode: NotNode) {
    notNode.operand.accept(this)
  }

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(this)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    variableDeclarationNode.scope.addLocalVariable(variableDeclarationNode.type, variableDeclarationNode.name)
    visit(variableDeclarationNode as VariableAssignmentNode)
  }

  fun drop2() {
    // TODO verify it does what I think
    mv.pop2Stack()
  }

  override fun visit(blockNode: BlockNode) {
    for (statement in blockNode.statements) {
      statement.accept(this)
    }
    val scope = blockNode.scope
    if (scope is InnerScope) {
      scope.clearInnerScopeLocalVariables()
    }
  }

  override fun visit(returnNode: ReturnNode) {
    // should never be called. Blocks will always take care of return statements and they will use pushArgument
    throw IllegalStateException("Compiler design error, sorry for that")
  }
}

private class PushingInstructionGenerator(override val mv: MethodBytecodeVisitor): IInstructionGenerator {
  lateinit var instructionGenerator: InstructionGenerator


  override fun visit(forStatement: ForStatement) {
    instructionGenerator.visit(forStatement)
  }

  override fun visit(forInStatement: ForInStatement) {
    instructionGenerator.visit(forInStatement)
  }
  override fun visit(rangeNode: RangeNode) {
    val methodName = if (rangeNode.fromExclusive && rangeNode.toExclusive) "ofExclusive"
    else if (rangeNode.fromExclusive) "ofFromExclusive"
    else if (rangeNode.toExclusive) "ofToExclusive"
    else "of"
    val method = ReflectJavaMethod(IntRanges::class.java.getMethod(methodName, Int::class.java, Int::class.java))
    if (rangeNode.from.type != JavaType.int || rangeNode.to.type != JavaType.int) {
      throw SemanticException("Only handle ranges for int value for now")
    }
    pushArgument(rangeNode.from)
    pushArgument(rangeNode.to)
    mv.invokeMethod(method)
  }

  override fun visit(notNode: NotNode) {
    when (notNode.operand.type) {
      JavaType.Boolean -> {
        pushArgument(notNode.operand)
        val method = ReflectJavaMethod(Class.forName("java.lang.Boolean").getMethod("booleanValue"))
        mv.invokeMethod(method)
      }
      JavaType.boolean -> {
        notNode.operand.accept(this)
      }
      else -> {
        throw SemanticException("Cannot negate something other than a boolean")
      }
    }
    mv.not()
  }
  override fun visit(whileStatement: WhileStatement) {
    instructionGenerator.visit(whileStatement)
  }
  override fun visit(ifStatementNode: IfStatementNode) {
    instructionGenerator.visit(ifStatementNode)
  }
  override fun visit(stringConstantNode: StringConstantNode) {
    mv.pushConstant(stringConstantNode.value)
  }

  override fun visit(breakLoopNode: BreakLoopNode) {
    instructionGenerator.visit(breakLoopNode)
  }

  override fun visit(continueLoopNode: ContinueLoopNode) {
    instructionGenerator.visit(continueLoopNode)
  }
  override fun visit(booleanExpression: BooleanExpressionNode) {
    if (booleanExpression.innerExpression.type == JavaType.boolean) {
      booleanExpression.innerExpression.accept(this)
    } else if (booleanExpression.innerExpression.type.primitive) {
      visit(BooleanConstantNode(true))
    } else {
      TODO("Doesn't handle yet Marcel truth for objects")
    }
  }
  override fun visit(toStringNode: ToStringNode) {
    val expr = toStringNode.expressionNode
    // TODO call Object.toString() method for non primitive type
    if (expr.type == JavaType.String) {
      expr.accept(this)
    } else {
      val argumentClass = expr.type.realClassOrObject
      if (argumentClass.isPrimitive) {
        val method = ReflectJavaMethod(String::class.java.getDeclaredMethod("valueOf", argumentClass))
        pushArgument(expr)
        mv.invokeMethod(method)
      } else {
        val method = ReflectJavaMethod(Object::class.java.getDeclaredMethod("toString", Object::class.java))
        pushArgument(expr)
        mv.invokeMethod(method)
      }
    }
  }
  override fun visit(stringNode: StringNode) {
    if (stringNode.parts.isEmpty()) {
      // empty string
      StringConstantNode("").accept(this)
      return
    } else if (stringNode.parts.size == 1) {
      ToStringNode(stringNode.parts.first()).accept(this)
      return
    }
    // new StringBuilder() can just provide an empty new scope as we'll just use it to extract the method from StringBuilder which already exists in the JDK
    visit(ConstructorCallNode(Scope(), JavaType(StringBuilder::class.java), mutableListOf()))
    for (part in stringNode.parts) {
      // chained calls
      val argumentClass = part.type.realClassOrObject
      val method = ReflectJavaMethod(StringBuilder::class.java.getDeclaredMethod("append", if (argumentClass.isPrimitive) argumentClass else Object::class.java))
      pushArgument(part)
      mv.invokeMethod(method)
    }
    val toStringMethod = ReflectJavaMethod(StringBuilder::class.java.getDeclaredMethod("toString"))
    mv.invokeMethod(toStringMethod)
  }

  override fun visit(integer: IntConstantNode) {
    mv.pushConstant(integer.value) // write primitive value, from an Object class e.g. Integer -> int
  }

  override fun visit(nullValueNode: NullValueNode) {
    mv.pushNull()
  }

  override fun visit(incrNode: IncrNode) {
    if (incrNode.returnValueBefore) {
      mv.pushVariable(incrNode.variableReference.scope, incrNode.variableReference.name)
      instructionGenerator.visit(incrNode)
    } else {
      instructionGenerator.visit(incrNode)
      mv.pushVariable(incrNode.variableReference.scope, incrNode.variableReference.name)
    }
  }
  override fun visit(booleanConstantNode: BooleanConstantNode) {
    mv.pushConstant(booleanConstantNode.value)
  }
  override fun visit(variableReferenceExpression: VariableReferenceExpression) {
    mv.pushVariable(variableReferenceExpression.scope, variableReferenceExpression.name)
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    super.visit(variableAssignmentNode)
    mv.pushVariable(variableAssignmentNode.scope, variableAssignmentNode.name)
  }

  override fun pushArgument(expr: ExpressionNode) {
    expr.accept(this)
  }

  override fun visit(operator: MulOperator) {
    super.visit(operator)
    mv.visitInsn((operator.type as JavaPrimitiveType).mulCode)
  }

  override fun visit(operator: DivOperator) {
    super.visit(operator)
    mv.visitInsn((operator.type as JavaPrimitiveType).divCode)
  }

  override fun visit(operator: MinusOperator) {
    super.visit(operator)
    mv.visitInsn((operator.type as JavaPrimitiveType).subCode)
  }


  override fun visit(operator: PlusOperator) {
    super.visit(operator)
    mv.visitInsn((operator.type as JavaPrimitiveType).addCode)
  }

  override fun visit(operator: PowOperator) {
    super.visit(operator)
    TODO("Implement pow, or call function?")
  }

  override fun visit(comparisonOperator: ComparisonOperatorNode) {
    super.visit(comparisonOperator)
    // TODO for now only handling primitive
    val endLabel = Label()
    val trueLabel = Label()
    mv.comparisonJump(comparisonOperator.operator, trueLabel)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.jumpTo(endLabel)
    mv.visitLabel(trueLabel)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitLabel(endLabel)
  }
  override fun visit(returnNode: ReturnNode) {
    returnNode.apply {
      if (!returnNode.scope.returnType.isAssignableFrom(expression.type)) {
        throw SemanticException("Cannot return ${expression.type} when return type is ${returnNode.scope.returnType}")
      }
    }
    returnNode.expression.accept(this)
  }

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(this)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    instructionGenerator.visit(variableDeclarationNode)
  }
  override fun visit(blockNode: BlockNode) {
    instructionGenerator.visit(blockNode)
  }

  override fun visit(blockNode: FunctionBlockNode) {
    instructionGenerator.visit(blockNode)
  }
}