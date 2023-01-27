package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import it.unimi.dsi.fastutil.ints.IntIterator
import marcel.lang.IntRanges
import marcel.lang.methods.MarcelTruth
import marcel.lang.runtime.BytecodeHelper
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes
import java.util.Optional
import java.util.OptionalDouble
import java.util.OptionalInt
import java.util.OptionalLong

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
    pushArgument(operator.boolExpression)
    val endLabel = Label()
    val falseLabel = Label()
    mv.jumpIfEq(falseLabel)
    operator.trueExpression.accept(this)
    mv.jumpTo(endLabel)
    mv.visitLabel(falseLabel)
    operator.falseExpression.accept(this)
    mv.visitLabel(endLabel)
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

  override fun visit(accessOperator: InvokeAccessOperator) {
    val access = accessOperator.rightOperand
    access.accept(this)
  }

  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) {
    val field = getFieldAccessOperator.fieldVariable
    if (!field.isStatic) {
      pushArgument(getFieldAccessOperator.leftOperand)
    }
    mv.getField(field)
  }

  private fun evaluateOperands(binaryOperatorNode: BinaryOperatorNode) {
    pushArgument(binaryOperatorNode.leftOperand)
    pushArgument(binaryOperatorNode.rightOperand)
  }

  override fun visit(fCall: FunctionCallNode) {
    val method = fCall.method
    val methodOwner = fCall.methodOwnerType
    if (!method.isStatic) {
      if (methodOwner is ExpressionNode) {
        pushArgument(methodOwner) // for instance method, we need to push owner
      } else {
        pushArgument(ReferenceExpression(fCall.scope, "this"))
      }
    }
    pushFunctionCallArguments(fCall)
    mv.invokeMethod(method)
  }

  private fun pushFunctionCallArguments(fCall: FunctionCallNode) {
    val method = fCall.method
    if (method.parameters.size != fCall.arguments.size) {
      throw SemanticException("Tried to call function $method with ${fCall.arguments.size} instead of ${method.parameters.size}")
    }
    for (i in method.parameters.indices) {
      val expectedType = method.parameters[i].type
      val actualType = fCall.arguments[i].type
      pushArgument(fCall.arguments[i])
      mv.castIfNecessaryOrThrow(expectedType, actualType)
    }
  }
  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    pushArgument(variableAssignmentNode.expression)
    val variable = variableAssignmentNode.scope.findVariable(variableAssignmentNode.name)
    // TODO if type is array and expected type is collection, handle casting in castIfNecessaryOrThrow method.
    //  for now only handle int arrays, and the other when we made sure it works
    mv.castIfNecessaryOrThrow(variable.type, variableAssignmentNode.expression.type)
    mv.storeInVariable(variable)
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
    if (forStatement.initStatement is VariableDeclarationNode) {
      val initStatementScope = (forStatement.initStatement as VariableDeclarationNode).scope as? InnerScope
      initStatementScope?.clearInnerScopeLocalVariables()
    }
  }

  override fun visit(forInStatement: ForInStatement) {
    val expression = forInStatement.inExpression
    if (!JavaType.of(Iterable::class.java).isAssignableFrom(expression.type)) {
      throw SemanticException("Only support for in Iterable")
    }
    // initialization
    val body = forInStatement.body
    val scope = body.scope
    scope.addLocalVariable(forInStatement.variableType, forInStatement.variableName)

    // creating iterator
    val iteratorVarName = "_tempIterator"
    val getIteratorMethod = expression.type.findMethodOrThrow("iterator", emptyList(), true)
    // get right method in function of types, to avoid auto-(un/debo)xing
    val methodName = if (JavaType.of(IntIterator::class.java).isAssignableFrom(getIteratorMethod.returnType)) "nextInt"
    else if (JavaType.of(IntIterator::class.java).isAssignableFrom(getIteratorMethod.returnType)) "next"
    else throw UnsupportedOperationException("wtf")
    visit(VariableDeclarationNode(scope, getIteratorMethod.returnType, iteratorVarName,
      FunctionCallNode(scope, "iterator", mutableListOf(), expression)))

    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition
    val iteratorVarReference = ReferenceExpression(scope, iteratorVarName)
    pushArgument(iteratorVarReference)
    mv.invokeMethod(IntIterator::class.java.getMethod("hasNext"))

    val loopEnd = Label()
    mv.jumpIfEq(loopEnd)

    // loop body
    visit(VariableAssignmentNode(scope, forInStatement.variableName, FunctionCallNode(scope, methodName, mutableListOf(), iteratorVarReference)))
    loopBody(forInStatement.body, loopStart, loopEnd)
    mv.jumpTo(loopStart)

    // loop end
    mv.visitLabel(loopEnd)
  }
  private fun loopBody(body: BlockNode, continueLabel: Label, breakLabel: Label) {
    val scope = body.scope as? InnerScope ?: throw RuntimeException("Compiler design bug")
    scope.continueLabel = continueLabel
    scope.breakLabel = breakLabel
    body.accept(this)
    scope.clearInnerScopeLocalVariables()
  }

  override fun visit(breakLoopNode: BreakLoopNode) {
    val label = breakLoopNode.scope.breakLabel ?: throw SemanticException("Cannot use break statement outside of a loop")
    mv.jumpTo(label)
  }

  override fun visit(continueLoopNode: ContinueLoopNode) {
    val label = continueLoopNode.scope.continueLabel ?: throw SemanticException("Cannot use continue statement outside of a loop")
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
    if (ifStatementNode.condition.innerExpression is TruthyVariableDeclarationNode) {
      val truthyExpression = ifStatementNode.condition.innerExpression as TruthyVariableDeclarationNode
      truthyExpression.scope.removeVariable(truthyExpression.name)
    }
  }

  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) {
    super.visit(getFieldAccessOperator)
    mv.popStack()
  }

  override fun visit(integer: IntConstantNode) {
    // don't need to write constants
  }

  override fun visit(longConstantNode: LongConstantNode) {
    // don't need to write constants
  }

  override fun visit(floatConstantNode: FloatConstantNode) {
    // don't need to write constants
  }

  override fun visit(doubleConstantNode: DoubleConstantNode) {
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

  override fun visit(literalListNode: LiteralArrayNode) {
    literalListNode.elements.forEach { it.accept(this) }
  }

  override fun visit(literalMapNode: LiteralMapNode) {
    literalMapNode.entries.forEach {
      it.first.accept(this)
      it.second.accept(this)
    }
  }
  override fun visit(booleanExpression: BooleanExpressionNode) {
    booleanExpression.innerExpression.accept(this)
  }

  override fun visit(incrNode: IncrNode) {
    if (incrNode.variableReference.type == JavaType.int) {
      mv.incr(incrNode.variableReference.variable, incrNode.amount)
    } else {
      TODO("Don't support other types than int for increment")
    }
  }

  override fun visit(nullValueNode: NullValueNode) {
    // no need to push anything
  }

  override fun visit(referenceExpression: ReferenceExpression) {
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
      if (lastStatement.type != JavaType.void) {
        pushArgument(lastStatement.expression)
        mv.castIfNecessaryOrThrow(blockNode.scope.returnType, lastStatement.expression.type)
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
    mv.pop2Stack()
  }

  override fun visit(operator: DivOperator) {
    super.visit(operator)
    mv.pop2Stack()
  }

  override fun visit(operator: MinusOperator) {
    super.visit(operator)
    mv.pop2Stack()
  }

  override fun visit(operator: PlusOperator) {
    super.visit(operator)
    mv.pop2Stack()
  }

  override fun visit(operator: PowOperator) {
    super.visit(operator)
    mv.pop2Stack()
  }

  override fun visit(comparisonOperatorNode: ComparisonOperatorNode) {
    super.visit(comparisonOperatorNode)
    mv.pop2Stack()
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

  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode) {
    visit(
      VariableDeclarationNode(truthyVariableDeclarationNode.scope, truthyVariableDeclarationNode.variableType,
      truthyVariableDeclarationNode.name, truthyVariableDeclarationNode.expression)
    )
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

  override fun visit(literalListNode: LiteralArrayNode) {
    mv.newArray(literalListNode.type, literalListNode.elements) {
      pushArgument(it)
    }
  }

  override fun visit(literalMapNode: LiteralMapNode) {
    var objectKeys = false
    val methodName = when (literalMapNode.type.raw()) {
      JavaType.int2ObjectMap -> "newInt2ObjectMap"
      JavaType.long2ObjectMap -> "newLong2ObjectMap"
      JavaType.float2ObjectMap -> "newFloat2ObjectMap"
      JavaType.double2ObjectMap -> "newDouble2ObjectMap"
      else -> {
        if (JavaType.of(Map::class.java).isAssignableFrom(literalMapNode.type.raw())) {
          objectKeys = true
          "newObject2ObjectMap"
        } else {
          throw SemanticException("Doesn't handle maps of type ${literalMapNode.type}")
        }
      }
    }
    mv.invokeMethod(BytecodeHelper::class.java.getDeclaredMethod(methodName))
    val keysType = literalMapNode.keysType
    val putMethodKeysType = if (keysType.primitive) keysType else JavaType.Object

    for (entry in literalMapNode.entries) {
      mv.dup()
      pushArgument(entry.first)
      if (objectKeys) {
        mv.castIfNecessaryOrThrow(JavaType.Object, entry.first.type)
      } else {
        mv.castIfNecessaryOrThrow(putMethodKeysType, entry.first.type)
      }
      pushArgument(entry.second)
      mv.castIfNecessaryOrThrow(JavaType.Object, entry.second.type)
      mv.invokeMethod(literalMapNode.type.findMethodOrThrow("put",
        listOf(putMethodKeysType, JavaType.Object)
      ))
      mv.popStack()
    }
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
    if (booleanExpression.innerExpression is NullValueNode) {
      visit(BooleanConstantNode(false))
    } else if (booleanExpression.innerExpression.type == JavaType.boolean
      || booleanExpression.innerExpression.type == JavaType.Boolean) {
      booleanExpression.innerExpression.accept(this)
      mv.castIfNecessaryOrThrow(JavaType.boolean, booleanExpression.innerExpression.type)
    } else if (booleanExpression.innerExpression.type.primitive) {
      // according to marcel truth, all primitive are truthy
      visit(BooleanConstantNode(true))
    } else {
      pushArgument(booleanExpression.innerExpression)
      mv.invokeMethod(MarcelTruth::class.java.getDeclaredMethod("truthy", Object::class.java))
    }
  }
  override fun visit(toStringNode: ToStringNode) {
    val expr = toStringNode.expressionNode
    if (expr.type == JavaType.String) {
      expr.accept(this)
    } else {
      val argumentType = expr.type
      if (argumentType.primitive) {
        val method = ReflectJavaMethod(String::class.java.getDeclaredMethod("valueOf", argumentType.realClazz))
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
    val type = JavaType.of(StringBuilder::class.java)
    visit(ConstructorCallNode(Scope(type), type, mutableListOf()))
    for (part in stringNode.parts) {
      // chained calls
      val argumentType = part.type
      val method = ReflectJavaMethod(StringBuilder::class.java.getDeclaredMethod("append",
        if (argumentType.primitive) argumentType.realClazz else JavaType.Object.realClazz))
      pushArgument(part)
      mv.invokeMethod(method)
    }
    val toStringMethod = ReflectJavaMethod(StringBuilder::class.java.getDeclaredMethod("toString"))
    mv.invokeMethod(toStringMethod)
  }

  override fun visit(integer: IntConstantNode) {
    mv.pushConstant(integer.value)
  }

  override fun visit(longConstantNode: LongConstantNode) {
    mv.pushConstant(longConstantNode.value)
  }

  override fun visit(floatConstantNode: FloatConstantNode) {
    mv.pushConstant(floatConstantNode.value)
  }

  override fun visit(doubleConstantNode: DoubleConstantNode) {
    mv.pushConstant(doubleConstantNode.value)
  }
  override fun visit(nullValueNode: NullValueNode) {
    mv.pushNull()
  }

  override fun visit(incrNode: IncrNode) {
    if (incrNode.returnValueBefore) {
      mv.pushVariable(incrNode.variableReference.variable)
      instructionGenerator.visit(incrNode)
    } else {
      instructionGenerator.visit(incrNode)
      mv.pushVariable(incrNode.variableReference.variable)
    }
  }
  override fun visit(booleanConstantNode: BooleanConstantNode) {
    mv.pushConstant(booleanConstantNode.value)
  }
  override fun visit(referenceExpression: ReferenceExpression) {
    mv.pushVariable(referenceExpression.variable)
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    super.visit(variableAssignmentNode)
    mv.pushVariable(variableAssignmentNode.scope.findVariable(variableAssignmentNode.name))
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
    mv.castIfNecessaryOrThrow(returnNode.scope.returnType, returnNode.expression.type)
  }

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(this)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    instructionGenerator.visit(variableDeclarationNode)
  }

  override fun visit(aTruthyVariableDeclarationNode: TruthyVariableDeclarationNode) {
    var truthyVariableDeclarationNode = aTruthyVariableDeclarationNode
    val variableType = truthyVariableDeclarationNode.variableType
    val expressionType = truthyVariableDeclarationNode.expression.type
    if (variableType.raw() != JavaType.of(Optional::class.java) && (
          listOf(Optional::class.java, OptionalInt::class.java, OptionalLong::class.java, OptionalDouble::class.java)
            .any {expressionType.raw().isAssignableFrom(JavaType.of(it)) }
          )) {
      truthyVariableDeclarationNode = TruthyVariableDeclarationNode(
        truthyVariableDeclarationNode.scope, truthyVariableDeclarationNode.variableType, truthyVariableDeclarationNode.name,
        InvokeAccessOperator(truthyVariableDeclarationNode.expression,
          FunctionCallNode(truthyVariableDeclarationNode.scope, "orElse", mutableListOf(NullValueNode(truthyVariableDeclarationNode.variableType)))
        )
      )
    }
    instructionGenerator.visit(truthyVariableDeclarationNode)
    if (truthyVariableDeclarationNode.variableType.primitive) {
      visit(BooleanConstantNode(true))
    } else {
      pushArgument(ReferenceExpression(truthyVariableDeclarationNode.scope, truthyVariableDeclarationNode.name))
      mv.invokeMethod(MarcelTruth::class.java.getDeclaredMethod("truthy", Object::class.java))
    }
  }
  override fun visit(blockNode: BlockNode) {
    instructionGenerator.visit(blockNode)
  }

  override fun visit(blockNode: FunctionBlockNode) {
    instructionGenerator.visit(blockNode)
  }
}