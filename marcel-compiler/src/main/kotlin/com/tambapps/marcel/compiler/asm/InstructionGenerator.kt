package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.LambdaHandler
import com.tambapps.marcel.compiler.util.getKeysType
import com.tambapps.marcel.compiler.util.getMethod
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.MultiVariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.scope.LambdaScope
import com.tambapps.marcel.parser.scope.LocalVariable
import com.tambapps.marcel.parser.scope.MethodField
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.scope.Variable
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import marcel.lang.primitives.iterators.IntIterator
import marcel.lang.IntRanges
import marcel.lang.lambda.Lambda1
import marcel.lang.methods.MarcelTruth
import marcel.lang.primitives.iterators.CharacterIterator
import marcel.lang.primitives.iterators.DoubleIterator
import marcel.lang.primitives.iterators.FloatIterator
import marcel.lang.primitives.iterators.LongIterator
import marcel.lang.runtime.BytecodeHelper
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.Closeable
import java.util.*

// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.if_icmp_cond
// https://asm.ow2.io/asm4-guide.pdf
// https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions
interface ArgumentPusher {
  fun pushArgument(expr: ExpressionNode)

}
private interface IInstructionGenerator: AstNodeVisitor<Unit>, ArgumentPusher {

  val mv: MethodBytecodeWriter
  val typeResolver: JavaTypeResolver
  val lambdaHandler: LambdaHandler


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


  override fun visit(switchNode: SwitchNode) {
    if (switchNode.branches.isEmpty()) {
      throw SemanticException("Switch must have at least one branch")
    }

    val elseBranchCount = switchNode.branches.count { it is ElseBranchNode }
    if (elseBranchCount > 1) {
      throw SemanticException("Can only have one else branch")
    }
    val switchType = switchNode.getType(typeResolver) // will always be Object because we don't handle generic return types for lambdas
    val actualSwitchType = JavaType.commonType(switchNode.branches.map { it.getType(typeResolver) })
    if (elseBranchCount == 0) {
      if (actualSwitchType.primitive) {
        throw SemanticException("Need to cover all cases (an else branch) for  switch returning primitives as they cannot be null")
      } else {
        // normally I should have specified the type for nullValueNode but since I don't handle generic return types properly it doesn't work
        switchNode.branches.add(ElseBranchNode(ExpressionStatementNode(NullValueNode())))
      }
    }
    if (elseBranchCount == 0 && switchType.primitive) {
      throw SemanticException("Need to cover all cases (an else branch) for  switch returning primitives as they cannot be null")
    }

    val elseBranch = switchNode.branches.find { it is ElseBranchNode  } as ElseBranchNode

    if (switchNode.branches.size == 1 && elseBranchCount > 0) {
      // if we only have an else branch, it will always be executed
      elseBranch.statementNode.accept(this)
      return
    }
    // if we want here, it means we'll generate a lambda. All branches need to return something
    elseBranch.returningLastStatement(switchNode.scope)

    val switchExpressionType = switchNode.expressionNode.getType(typeResolver)

    val parameters = listOf(MethodParameter(switchExpressionType, "it"))
    val lambdaScope = LambdaScope(switchNode.scope)
    // since the return type of a lambda is generic, it must always be an object
    val lambdaMethodScope = MethodScope(lambdaScope, "invoke", parameters, switchType)

    val switchExpressionReference = ReferenceExpression(lambdaMethodScope, "it")
    val branches = switchNode.branches.filter { it !is ElseBranchNode }
    branches.forEach { it.returningLastStatement(switchNode.scope) }
    // marcel switch is just an if/elsif
    val rootIf = switchBranchToIf(switchExpressionReference, branches.first())
    var currentIf = rootIf
    for (i in 1..branches.lastIndex) {
      val branch = branches[i]
      if (branch == elseBranch) continue
      val newIfBranch = switchBranchToIf(switchExpressionReference, branch)
      currentIf.falseStatementNode = newIfBranch
      currentIf = newIfBranch
    }
    currentIf.falseStatementNode = elseBranch.statementNode

    // TODO lambda invoke method body is weirdly generated
    val lambdaNode = LambdaNode(lambdaScope, parameters,
      BlockNode(lambdaMethodScope, mutableListOf(rootIf)))

    val lambdaType = lambdaHandler.defineLambda(lambdaNode)
    visit(ConstructorCallNode(lambdaNode.scope, lambdaType, mutableListOf()))
    pushArgument(switchNode.expressionNode)
    mv.castIfNecessaryOrThrow(JavaType.Object, switchExpressionType)
    mv.invokeMethod(typeResolver.findMethodOrThrow(Lambda1::class.javaType, "invoke", listOf(JavaType.Object)))
  }

  private fun switchBranchToIf(switchExpression: ExpressionNode, branchNode: SwitchBranchNode): IfStatementNode {
    return when (branchNode) {
      is EqSwitchBranchNode -> IfStatementNode(ComparisonOperatorNode(ComparisonOperator.EQUAL, switchExpression, branchNode.valueExpression),
        branchNode.statementNode, null)
      else -> throw RuntimeException("Compiler error. Doesn't handle ${branchNode.javaClass.simpleName} switch branches")
    }
  }
  override fun visit(switchBranch: SwitchBranchNode) {
    throw RuntimeException("Compiler error. Shouldn't happen")
  }

  override fun visit(ifStatementNode: IfStatementNode) {
    pushArgument(ifStatementNode.condition)
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
      truthyExpression.scope.freeVariable(truthyExpression.name)
    }
  }

  override fun visit(elvisOperator: ElvisOperator) {
    visit(TernaryNode(
      BooleanExpressionNode(elvisOperator.leftOperand),
      elvisOperator.leftOperand, elvisOperator.rightOperand
    ))
  }

  override fun visit(fCall: ConstructorCallNode) {
    if (fCall.type.primitive) {
      throw SemanticException("Cannot instantiate a primitive type")
    }
    mv.visitConstructorCall(fCall)
  }

  override fun visit(fCall: SuperConstructorCallNode) {
    mv.visitSuperConstructorCall(fCall)
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

  override fun visit(leftShiftOperator: LeftShiftOperator) {
    marcelOperator(leftShiftOperator, "leftShift")
  }

  override fun visit(rightShiftOperator: RightShiftOperator) {
    marcelOperator(rightShiftOperator, "rightShift")
  }

  fun marcelOperator(binaryOperatorNode: BinaryOperatorNode, operatorMethodName: String): JavaType {
    val type1 = binaryOperatorNode.leftOperand.getType(typeResolver)
    if (type1.primitive) {
      throw SemanticException("Doesn't support left shirt operator for primitive type")
    }
    val leftShiftMethod = typeResolver.findMethodOrThrow(type1, operatorMethodName, listOf(binaryOperatorNode.rightOperand.getType(typeResolver)))
    pushArgument(binaryOperatorNode.leftOperand)
    mv.invokeMethodWithArguments(leftShiftMethod, binaryOperatorNode.rightOperand)
    return leftShiftMethod.returnType
  }

  override fun visit(comparisonOperatorNode: ComparisonOperatorNode) {
    val leftOperand = comparisonOperatorNode.leftOperand
    val rightOperand = comparisonOperatorNode.rightOperand
    val endLabel = Label()
    val trueLabel = Label()
    val operator = comparisonOperatorNode.operator
    var objectcomparison = false
    if (!leftOperand.getType(typeResolver).primitive || !rightOperand.getType(typeResolver).primitive) {
      pushArgument(leftOperand)
      mv.castIfNecessaryOrThrow(JavaType.Object, leftOperand.getType(typeResolver))
      pushArgument(rightOperand)
      mv.castIfNecessaryOrThrow(JavaType.Object, rightOperand.getType(typeResolver))
      if ((leftOperand is NullValueNode || rightOperand is NullValueNode)) {
        objectcomparison = true
        if (operator != ComparisonOperator.EQUAL && operator != ComparisonOperator.NOT_EQUAL) {
          throw SemanticException("Cannot compare null value with ${operator.symbolString} operator")
        }
      } else {
        when (operator) {
          ComparisonOperator.EQUAL, ComparisonOperator.NOT_EQUAL -> {
            mv.invokeMethod(BytecodeHelper::class.java.getDeclaredMethod("objectsEqual", JavaType.Object.realClazz, JavaType.Object.realClazz))
            if (operator == ComparisonOperator.NOT_EQUAL) mv.not()
            return // the above method returns a boolean
          }
          else -> {
            val method = typeResolver.findMethodOrThrow(leftOperand.getType(typeResolver), "compareTo", listOf(rightOperand.getType(typeResolver)))
            if (method.returnType != JavaType.int) throw SemanticException("compareTo method should return an int in order to be used in comparator")
            mv.invokeMethod(method)
            mv.pushConstant(0) // pushing 0 because we're comparing two numbers below
          }
        }
      }
    } else {
      if (leftOperand.getType(typeResolver) != JavaType.int || rightOperand.getType(typeResolver) != JavaType.int) {
        val otherType = if (leftOperand.getType(typeResolver) != JavaType.int) leftOperand.getType(typeResolver) else rightOperand.getType(typeResolver)
        pushArgument(leftOperand)
        mv.castIfNecessaryOrThrow(otherType, leftOperand.getType(typeResolver))
        pushArgument(rightOperand)
        mv.castIfNecessaryOrThrow(otherType, rightOperand.getType(typeResolver))
        when (otherType) {
          JavaType.double -> mv.visitInsn(Opcodes.DCMPL)
          JavaType.float -> mv.visitInsn(Opcodes.FCMPL)
          JavaType.long -> mv.visitInsn(Opcodes.LCMP)
          else -> throw UnsupportedOperationException("Doesn't handle comparison of primitive type $otherType")
        }
        mv.pushConstant(0) // pushing 0 because we're comparing two numbers below
      } else {
        evaluateOperands(comparisonOperatorNode)
      }
    }
    mv.jump(if (objectcomparison) comparisonOperatorNode.operator.objectOpCode else comparisonOperatorNode.operator.iOpCode, trueLabel)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.jumpTo(endLabel)
    mv.visitLabel(trueLabel)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitLabel(endLabel)
  }

  override fun visit(andOperator: AndOperator) {
    val labelFalse = Label()
    val labelEnd = Label()
    pushArgument(andOperator.leftOperand)
    mv.jumpIfEq(labelFalse)
    pushArgument(andOperator.rightOperand)
    mv.jumpIfEq(labelFalse)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.jumpTo(labelEnd)
    mv.visitLabel(labelFalse)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitLabel(labelEnd)
  }

  override fun visit(orOperator: OrOperator) {
    val labelTrue = Label()
    val labelFalse = Label()
    val labelEnd = Label()
    pushArgument(orOperator.leftOperand)
    mv.jumpIfNe(labelTrue)
    pushArgument(orOperator.rightOperand)
    mv.jumpIfEq(labelFalse)
    mv.visitLabel(labelTrue)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.jumpTo(labelEnd)
    mv.visitLabel(labelFalse)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitLabel(labelEnd)
  }

  override fun visit(accessOperator: InvokeAccessOperator) {
    val access = accessOperator.rightOperand
    access.methodOwnerType = accessOperator.leftOperand
    access.accept(this)
  }

  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) {
    val field = typeResolver.findFieldOrThrow(getFieldAccessOperator.leftOperand.getType(typeResolver), getFieldAccessOperator.rightOperand.name)
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
    val method = fCall.getMethod(typeResolver)
    val methodOwner = fCall.methodOwnerType
    if (method.isInline) {
      val inlineMethod = method as MethodNode
      val innerScope = InnerScope(
        fCall.scope as? MethodScope ?: throw SemanticException("Can only call inline functions in a method"))
      val inlineBlock = inlineMethod.block.asSimpleBlock(innerScope)
      inlineBlock.trySetTreeScope(innerScope)
      // initializing arguments
      if (fCall.arguments.size != inlineMethod.parameters.size) {
        throw SemanticException("Invalid number of arguments for method ${method.name}")
      }
      val variables = method.parameters.map { innerScope.addLocalVariable(it.type, it.name) }
      for (i in variables.indices) {
        visit(VariableAssignmentNode(innerScope, variables[i].name, fCall.arguments[i]))
      }
      visit(inlineBlock)
      innerScope.clearInnerScopeLocalVariables()
    } else {
      if (!method.isStatic) {
        if (methodOwner is ExpressionNode) {
          pushArgument(methodOwner) // for instance method, we need to push owner
        } else {
          pushArgument(ReferenceExpression(fCall.scope, "this"))
        }
      }
      mv.invokeMethodWithArguments(method, fCall.arguments)
    }
  }

  override fun visit(lambdaNode: LambdaNode) {
    val lambdaType = lambdaHandler.defineLambda(lambdaNode)
    visit(ConstructorCallNode(lambdaNode.scope, lambdaType, mutableListOf()))
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    val variable = variableAssignmentNode.scope.findVariable(variableAssignmentNode.name)
    pushAssignementExpression(variable, variableAssignmentNode.expression)
    mv.storeInVariable(variable)
  }

  private fun pushAssignementExpression(variable: Variable, expr: ExpressionNode) {
    var expression = expr
    val variableType = variable.type
    if (expression is LiteralArrayNode && expression.elements.isEmpty()) {
      val elementsType = if (variableType is JavaArrayType) variableType.elementsType
      else if (JavaType.intList.isAssignableFrom(variableType) || JavaType.intSet.isAssignableFrom(variableType)) JavaType.int
      else if (JavaType.longList.isAssignableFrom(variableType) || JavaType.longSet.isAssignableFrom(variableType)) JavaType.long
      else if (JavaType.floatList.isAssignableFrom(variableType) || JavaType.floatSet.isAssignableFrom(variableType)) JavaType.float
      else if (JavaType.doubleList.isAssignableFrom(variableType) || JavaType.doubleSet.isAssignableFrom(variableType)) JavaType.double
      else throw SemanticException("Couldn't guess type of empty array. You can explicitly specify your wanted type with the 'as' keyword (e.g. '[] as int[]')")
      expression = EmptyArrayNode(JavaType.arrayType(elementsType))
    } else if (variableType.isInterface && expression is LambdaNode) {
      expression.interfaceType = variableType
    }
    pushArgument(expression)
    mv.castIfNecessaryOrThrow(variable.type, expression.getType(typeResolver))
  }
  override fun visit(fieldAssignmentNode: FieldAssignmentNode) {
    val fieldVariable = typeResolver.findFieldOrThrow(
      fieldAssignmentNode.fieldNode.leftOperand.getType(typeResolver),
      fieldAssignmentNode.fieldNode.rightOperand.name
    )
    if (fieldVariable is MethodField && !fieldVariable.isStatic) {
      pushArgument(fieldAssignmentNode.fieldNode.leftOperand)
    }
    pushAssignementExpression(fieldVariable, fieldAssignmentNode.expression)
    mv.storeInVariable(fieldVariable)
  }

  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode) {
    val indexedReference = indexedVariableAssignmentNode.indexedReference
    mv.storeInVariablePutAt(
      indexedReference.scope, indexedReference.variable,
      indexedReference.indexArguments, indexedVariableAssignmentNode.expression
    )
  }
  override fun visit(indexedReferenceExpression: IndexedReferenceExpression) {
    if (indexedReferenceExpression.isSafeIndex) {
       TODO("push a ternary that checking if index is between 0 and size()")
    }
    mv.pushVariableGetAt(indexedReferenceExpression.scope, indexedReferenceExpression.variable,
      indexedReferenceExpression.indexArguments)
  }

  override fun visit(voidExpression: VoidExpression) {
    // do nothing, it's void
  }
}

/**
 * Generates expression bytecode but don't push them to the stack. (Useful for statement expressions)
 */
class InstructionGenerator(
  classNode: ClassNode,
  methodNode: MethodNode,
  override val typeResolver: JavaTypeResolver,
  methodVisitor: MethodVisitor
):
  IInstructionGenerator {

  override val mv = MethodBytecodeWriter(methodVisitor, typeResolver)
  override val lambdaHandler = LambdaHandler(classNode, methodNode)

  private val pushingInstructionGenerator = PushingInstructionGenerator(typeResolver, mv, lambdaHandler)
  init {
    mv.argumentPusher = this
    pushingInstructionGenerator.mv.argumentPusher = pushingInstructionGenerator
  }
  init {
    pushingInstructionGenerator.instructionGenerator = this
  }

  override fun marcelOperator(binaryOperatorNode: BinaryOperatorNode, operatorMethodName: String): JavaType {
    val type = super.marcelOperator(binaryOperatorNode, operatorMethodName)
    if (type != JavaType.void) {
      mv.popStack()
    }
    return type
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
    whileStatement.scope.clearInnerScopeLocalVariables()
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
    forStatement.scope.clearInnerScopeLocalVariables()
  }

  override fun visit(forInStatement: ForInStatement) {
    val expression = forInStatement.inExpression
    val expressionType = expression.getType(typeResolver)

    // initialization
    val scope = forInStatement.scope
    scope.addLocalVariable(forInStatement.variableType, forInStatement.variableName)

    // creating iterator
    val iteratorExpression = if (Iterable::class.javaType.isAssignableFrom(expressionType)) {
      FunctionCallNode(scope, "iterator", mutableListOf(), expression)
    } else if (Iterator::class.javaType.isAssignableFrom(expressionType)) expression
    else throw SemanticException("Doesn't handle iterating on $expressionType")
    val iteratorExpressionType = iteratorExpression.getType(typeResolver)

    val iteratorVariable = scope.addLocalVariable(iteratorExpressionType)

    // get right method in function of types, to avoid auto-(un/debo)xing
    val methodName = if (IntIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextInt"
    else if (LongIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextLong"
    else if (FloatIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextFloat"
    else if (DoubleIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextDouble"
    else if (CharacterIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextCharacter"
    else if (Iterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "next"
    else throw UnsupportedOperationException("wtf")
    visit(VariableAssignmentNode(scope, iteratorVariable.name, iteratorExpression))

    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition
    val iteratorVarReference = ReferenceExpression(scope, iteratorVariable.name)
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

    if (Closeable::class.javaType.isAssignableFrom(iteratorExpressionType)) {
      // TODO would need to be in a finally block (which means the loop should be in a try block)
      pushArgument(iteratorVarReference)
      mv.invokeMethod(Closeable::class.java.getMethod("close"))
    }
    scope.clearInnerScopeLocalVariables()
    scope.freeVariable(iteratorVariable.name)
  }
  private fun loopBody(body: BlockNode, continueLabel: Label, breakLabel: Label) {
    val scope = body.scope as? InnerScope ?: throw RuntimeException("Compiler design bug")
    scope.continueLabel = continueLabel
    scope.breakLabel = breakLabel
    body.accept(this)
  }

  override fun visit(breakLoopNode: BreakLoopNode) {
    val label = breakLoopNode.scope.breakLabel ?: throw SemanticException("Cannot use break statement outside of a loop")
    mv.jumpTo(label)
  }

  override fun visit(continueLoopNode: ContinueLoopNode) {
    val label = continueLoopNode.scope.continueLabel ?: throw SemanticException("Cannot use continue statement outside of a loop")
    mv.jumpTo(label)
  }

  override fun visit(lambdaNode: LambdaNode) {
    super.visit(lambdaNode)
    mv.popStack()
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

  override fun visit(charNode: CharNode) {
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
    if (fCall.getType(typeResolver) != JavaType.void) {
      mv.popStack() // don't really know if it's necessary
    }
  }
  override fun visit(toStringNode: ToStringNode) {
    toStringNode.expressionNode.accept(this)
  }

  override fun visit(asNode: AsNode) {
    val expression = asNode.expressionNode
    if (expression is LiteralArrayNode && expression.elements.isEmpty()) {
      visit(EmptyArrayNode(asNode.type as? JavaArrayType ?: throw SemanticException("Can only convert empty arrays to array types using 'as' keyword")))
    } else {
      asNode.expressionNode.accept(this)
    }
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
    if (incrNode.variableReference.getType(typeResolver) == JavaType.int && incrNode.variableReference.variable is LocalVariable) {
      mv.incrLocalVariable(incrNode.variableReference.variable as LocalVariable, incrNode.amount)
    } else {
      val ref = incrNode.variableReference
      visit(VariableAssignmentNode(ref.scope, ref.name, PlusOperator(ref, IntConstantNode(incrNode.amount))))
    }
  }

  override fun visit(nullValueNode: NullValueNode) {
    // no need to push anything
  }

  override fun visit(referenceExpression: ReferenceExpression) {
    // don't need to push value to the stack by default
  }

  override fun visit(indexedReferenceExpression: IndexedReferenceExpression) {
    super.visit(indexedReferenceExpression)
    mv.popStack()
  }

  override fun pushArgument(expr: ExpressionNode) {
    pushingInstructionGenerator.pushArgument(expr)
  }


  override fun visit(blockNode: FunctionBlockNode) {
    for (i in 0..(blockNode.statements.size - 2)) {
      blockNode.statements[i].accept(this)
    }
    val lastStatement = blockNode.statements.lastOrNull()
    if (lastStatement is ReturnNode) {
      // if this is return node, there is no ambiguity here. Just process it
      lastStatement.accept(this)
      return
    }
    if (blockNode.scope.returnType == JavaType.void) {
      lastStatement?.accept(this)
      mv.returnVoid()
    } else {
      if (lastStatement is ExpressionStatementNode && lastStatement.expression.getType(typeResolver) != JavaType.void) {
        visit(ReturnNode(blockNode.scope, lastStatement.expression))
      } else if (!blockNode.scope.returnType.primitive) {
        lastStatement?.accept(this)
        // just return null
        mv.pushNull()
        mv.returnCode(blockNode.scope.returnType.returnCode)
      } else {
        throw SemanticException("Function returning primitive types must explicitly return something as the last statement. (Someday maybe this will be checked a littler smarter)")
      }
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
    mv.popStack()
  }

  override fun visit(andOperator: AndOperator) {
    super.visit(andOperator)
    mv.popStack()
  }

  override fun visit(orOperator: OrOperator) {
    super.visit(orOperator)
    mv.popStack()
  }

  override fun visit(notNode: NotNode) {
    notNode.operand.accept(this)
  }

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(this)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    variableDeclarationNode.scope.addLocalVariable(variableDeclarationNode.type, variableDeclarationNode.name,
      variableDeclarationNode.isFinal)
    visit(variableDeclarationNode as VariableAssignmentNode)
  }

  override fun visit(multiVariableDeclarationNode: MultiVariableDeclarationNode) {
    val scope = multiVariableDeclarationNode.scope
    val expressionType = multiVariableDeclarationNode.expression.getType(typeResolver)
    if (!List::class.javaType.isAssignableFrom(expressionType) && !expressionType.isArray) {
      throw SemanticException("Multi variable declarations must use an array or a list as the expression")
    }
    val tempVar = scope.addLocalVariable(expressionType)
    // assign expression to variable
    visit(VariableAssignmentNode(scope, tempVar.name, multiVariableDeclarationNode.expression))
    // then process each variable declarations
    for (i in multiVariableDeclarationNode.declarations.indices) {
      val declaration = multiVariableDeclarationNode.declarations[i]
      visit(VariableDeclarationNode(scope, declaration.first, declaration.second, false,
        IndexedReferenceExpression(scope, tempVar.name, listOf(IntConstantNode(i)), false)))
    }
    scope.freeVariable(tempVar.name)
  }

  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode) {
    visit(
      VariableDeclarationNode(truthyVariableDeclarationNode.scope, truthyVariableDeclarationNode.variableType,
      truthyVariableDeclarationNode.name, false, truthyVariableDeclarationNode.expression)
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
    if (returnNode.expression != null && returnNode.scope.returnType == JavaType.void) {
      throw SemanticException("Cannot return an expression in a void function")
    }
    pushArgument(returnNode.expression)
    mv.castIfNecessaryOrThrow(returnNode.scope.returnType, returnNode.expression.getType(typeResolver))
    mv.returnCode(returnNode.scope.returnType.returnCode)

  }
}

private class PushingInstructionGenerator(
  override val typeResolver: JavaTypeResolver,
  override val mv: MethodBytecodeWriter,
  override val lambdaHandler: LambdaHandler,
  ): IInstructionGenerator {
  lateinit var instructionGenerator: InstructionGenerator


  override fun visit(forStatement: ForStatement) {
    instructionGenerator.visit(forStatement)
  }

  override fun visit(forInStatement: ForInStatement) {
    instructionGenerator.visit(forInStatement)
  }

  override fun visit(literalListNode: LiteralArrayNode) {
    mv.newArray(literalListNode.getType(typeResolver).asArrayType, literalListNode.elements)
  }

  override fun visit(literalMapNode: LiteralMapNode) {
    var objectKeys = false
    val methodName = when (literalMapNode.getType(typeResolver).raw()) {
      JavaType.int2ObjectMap -> "newInt2ObjectMap"
      JavaType.long2ObjectMap -> "newLong2ObjectMap"
      JavaType.char2ObjectMap -> "newChar2ObjectMap"
      else -> {
        if (Map::class.javaType.isAssignableFrom(literalMapNode.getType(typeResolver).raw())) {
          objectKeys = true
          "newObject2ObjectMap"
        } else {
          throw SemanticException("Doesn't handle maps of type ${literalMapNode.getType(typeResolver)}")
        }
      }
    }
    mv.invokeMethod(BytecodeHelper::class.java.getDeclaredMethod(methodName))
    val keysType = literalMapNode.getKeysType(typeResolver)
    val putMethodKeysType = if (keysType.primitive) keysType else JavaType.Object

    for (entry in literalMapNode.entries) {
      mv.dup()
      pushArgument(entry.first)
      if (objectKeys) {
        mv.castIfNecessaryOrThrow(JavaType.Object, entry.first.getType(typeResolver))
      } else {
        mv.castIfNecessaryOrThrow(putMethodKeysType, entry.first.getType(typeResolver))
      }
      pushArgument(entry.second)
      mv.castIfNecessaryOrThrow(JavaType.Object, entry.second.getType(typeResolver))
      mv.invokeMethod(typeResolver.findMethodOrThrow(literalMapNode.getType(typeResolver),
        "put",
        listOf(putMethodKeysType, JavaType.Object)
      ))
      mv.popStack()
    }
  }

  override fun visit(rangeNode: RangeNode) {
    // TODO only handle int ranges but not other types
    val methodName = if (rangeNode.fromExclusive && rangeNode.toExclusive) "ofExclusive"
    else if (rangeNode.fromExclusive) "ofFromExclusive"
    else if (rangeNode.toExclusive) "ofToExclusive"
    else "of"
    val method = ReflectJavaMethod(IntRanges::class.java.getMethod(methodName, Int::class.java, Int::class.java))
    if (rangeNode.from.getType(typeResolver) != JavaType.int || rangeNode.to.getType(typeResolver) != JavaType.int) {
      throw SemanticException("Only handle ranges for int value for now")
    }
    mv.invokeMethodWithArguments(method, rangeNode.from, rangeNode.to)
  }

  override fun visit(notNode: NotNode) {
    when (notNode.operand.getType(typeResolver)) {
      JavaType.Boolean -> mv.invokeMethodWithArguments(typeResolver.findMethodOrThrow(JavaType.Boolean, "booleanValue", emptyList()), notNode.operand)
      JavaType.boolean -> notNode.operand.accept(this)
      else -> throw SemanticException("Cannot negate something other than a boolean")
    }
    mv.not()
  }
  override fun visit(whileStatement: WhileStatement) {
    instructionGenerator.visit(whileStatement)
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
    } else if (booleanExpression.innerExpression.getType(typeResolver) == JavaType.boolean
      || booleanExpression.innerExpression.getType(typeResolver) == JavaType.Boolean) {
      booleanExpression.innerExpression.accept(this)
      mv.castIfNecessaryOrThrow(JavaType.boolean, booleanExpression.innerExpression.getType(typeResolver))
    } else if (booleanExpression.innerExpression.getType(typeResolver).primitive) {
      // according to marcel truth, all primitive are truthy
      booleanExpression.innerExpression.accept(instructionGenerator)
      visit(BooleanConstantNode(true))
    } else {
      val truthyMethod = typeResolver.findMethodOrThrow(MarcelTruth::class.javaType, "truthy", listOf(booleanExpression.innerExpression.getType(typeResolver)))
      mv.invokeMethodWithArguments(truthyMethod, booleanExpression.innerExpression)
    }
  }

  override fun visit(asNode: AsNode) {
    asNode.expressionNode.accept(this)
    mv.castIfNecessaryOrThrow(asNode.type, asNode.expressionNode.getType(typeResolver))
  }

  override fun visit(toStringNode: ToStringNode) {
    val expr = toStringNode.expressionNode
    if (expr.getType(typeResolver) == JavaType.String) {
      expr.accept(this)
    } else {
      val argumentType = expr.getType(typeResolver)
      if (argumentType.primitive) {
        mv.invokeMethodWithArguments(String::class.java.getDeclaredMethod("valueOf", argumentType.realClazz), expr)
      } else {
        mv.invokeMethodWithArguments(Object::class.java.getDeclaredMethod("toString"), expr)
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
    val type = StringBuilder::class.javaType
    visit(ConstructorCallNode(Scope(typeResolver, type), type, mutableListOf()))
    for (part in stringNode.parts) {
      // chained calls
      val argumentType = part.getType(typeResolver)
      val method = ReflectJavaMethod(StringBuilder::class.java.getDeclaredMethod("append",
        if (argumentType.primitive) argumentType.realClazz else JavaType.Object.realClazz))
      mv.invokeMethodWithArguments(method, part)
    }
    mv.invokeMethod(StringBuilder::class.java.getDeclaredMethod("toString"))
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

  override fun visit(charNode: CharNode) {
    val value = charNode.value
    if (value.length != 1) throw SemanticException("Characters should be strings of exactly one char")
    mv.pushConstant(value[0])
  }
  override fun visit(nullValueNode: NullValueNode) {
    mv.pushNull()
  }

  override fun visit(incrNode: IncrNode) {
    if (incrNode.returnValueBefore) {
      mv.pushVariable(incrNode.variableReference.scope, incrNode.variableReference.variable)
      instructionGenerator.visit(incrNode)
    } else {
      instructionGenerator.visit(incrNode)
      mv.pushVariable(incrNode.variableReference.scope, incrNode.variableReference.variable)
    }
  }
  override fun visit(booleanConstantNode: BooleanConstantNode) {
    mv.pushConstant(booleanConstantNode.value)
  }
  override fun visit(referenceExpression: ReferenceExpression) {
    mv.pushVariable(referenceExpression.scope, referenceExpression.variable)
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    super.visit(variableAssignmentNode)
    mv.pushVariable(variableAssignmentNode.scope, variableAssignmentNode.scope.findVariable(variableAssignmentNode.name))
  }

  override fun visit(fieldAssignmentNode: FieldAssignmentNode) {
    super.visit(fieldAssignmentNode)
    val field = typeResolver.findFieldOrThrow(fieldAssignmentNode.fieldNode.leftOperand.getType(typeResolver), fieldAssignmentNode.fieldNode.rightOperand.name)
    mv.pushVariable(fieldAssignmentNode.scope, field)
  }

  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode) {
    super.visit(indexedVariableAssignmentNode)
    pushArgument(indexedVariableAssignmentNode.indexedReference)
  }
  override fun pushArgument(expr: ExpressionNode) {
    expr.accept(this)
  }

  override fun visit(operator: MulOperator) {
    super.visit(operator)
    mv.visitInsn(operator.getType(typeResolver).asPrimitiveType.mulCode)
  }

  override fun visit(operator: DivOperator) {
    super.visit(operator)
    mv.visitInsn(operator.getType(typeResolver).asPrimitiveType.divCode)
  }

  override fun visit(operator: MinusOperator) {
    super.visit(operator)
    mv.visitInsn(operator.getType(typeResolver).asPrimitiveType.subCode)
  }


  override fun visit(operator: PlusOperator) {
    val operatorType = operator.getType(typeResolver)
    if (operatorType.primitive) {
      super.visit(operator)
      mv.visitInsn(operator.getType(typeResolver).asPrimitiveType.addCode)
    } else if (operator.leftOperand.getType(typeResolver) == JavaType.String || operator.rightOperand.getType(typeResolver) == JavaType.String) {
      visit(StringNode(listOf(operator.leftOperand, operator.rightOperand)))
    } else {
      TODO("Doesn't handle custom + operator yet")
    }
  }

  override fun visit(operator: PowOperator) {
    super.visit(operator)
    throw UnsupportedOperationException("Doesn't handle power operator for now (or ever?)")
  }

  override fun visit(returnNode: ReturnNode) {
    returnNode.apply {
      if (!returnNode.scope.returnType.isAssignableFrom(expression.getType(typeResolver))) {
        throw SemanticException("Cannot return ${expression.getType(typeResolver)} when return type is ${returnNode.scope.returnType}")
      }
    }
    returnNode.expression.accept(this)
    mv.castIfNecessaryOrThrow(returnNode.scope.returnType, returnNode.expression.getType(typeResolver))
  }

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(this)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    instructionGenerator.visit(variableDeclarationNode)
  }

  override fun visit(multiVariableDeclarationNode: MultiVariableDeclarationNode) {
    instructionGenerator.visit(multiVariableDeclarationNode)
  }
  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode) {
    var actualTruthyVariableDeclarationNode = truthyVariableDeclarationNode
    val variableType = actualTruthyVariableDeclarationNode.variableType
    val expressionType = actualTruthyVariableDeclarationNode.expression.getType(typeResolver)
    if (variableType.raw() != Optional::class.javaType && (
          listOf(Optional::class.javaType, OptionalInt::class.javaType, OptionalLong::class.javaType, OptionalDouble::class.javaType)
            .any {expressionType.raw().isAssignableFrom(it) }
          )) {
      actualTruthyVariableDeclarationNode = TruthyVariableDeclarationNode(
        actualTruthyVariableDeclarationNode.scope, actualTruthyVariableDeclarationNode.variableType, actualTruthyVariableDeclarationNode.name,
        InvokeAccessOperator(actualTruthyVariableDeclarationNode.expression,
          FunctionCallNode(actualTruthyVariableDeclarationNode.scope, "orElse", mutableListOf(NullValueNode(actualTruthyVariableDeclarationNode.variableType)))
        )
      )
    }
    instructionGenerator.visit(actualTruthyVariableDeclarationNode)
    if (actualTruthyVariableDeclarationNode.variableType.primitive) {
      visit(BooleanConstantNode(true))
    } else {
      pushArgument(ReferenceExpression(actualTruthyVariableDeclarationNode.scope, actualTruthyVariableDeclarationNode.name))
      val truthyMethod = typeResolver.findMethodOrThrow(MarcelTruth::class.javaType, "truthy", listOf(actualTruthyVariableDeclarationNode.expression.getType(typeResolver)))
      mv.invokeMethod(truthyMethod)
    }
  }
  override fun visit(blockNode: BlockNode) {
    instructionGenerator.visit(blockNode)
  }

  override fun visit(blockNode: FunctionBlockNode) {
    instructionGenerator.visit(blockNode)
  }
}