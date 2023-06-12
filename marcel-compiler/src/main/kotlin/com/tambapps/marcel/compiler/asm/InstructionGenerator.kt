package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.LambdaHandler
import com.tambapps.marcel.compiler.util.getKeysType
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.ast.MethodParameter
import com.tambapps.marcel.parser.ast.AstInstructionNode
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.MethodParameterNode
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.BlockStatement
import com.tambapps.marcel.parser.ast.statement.BreakLoopNode
import com.tambapps.marcel.parser.ast.statement.ContinueLoopNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForInStatement
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.MultiVariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.ast.statement.TryCatchNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.BoundField
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.scope.LocalVariable
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.scope.Variable
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import marcel.lang.DynamicObject
import marcel.lang.IntRanges
import marcel.lang.LongRanges
import marcel.lang.methods.MarcelTruth
import marcel.lang.primitives.iterators.CharacterIterator
import marcel.lang.primitives.iterators.DoubleIterator
import marcel.lang.primitives.iterators.FloatIterator
import marcel.lang.primitives.iterators.IntIterator
import marcel.lang.primitives.iterators.LongIterator
import marcel.lang.runtime.BytecodeHelper
import marcel.lang.util.CharSequenceIterator
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.Closeable
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.if_icmp_cond
// https://asm.ow2.io/asm4-guide.pdf
// https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions
interface ArgumentPusher {
  fun pushArgument(expr: ExpressionNode)
  fun visitWithoutPushing(astNode: AstInstructionNode)

}
private interface IInstructionGenerator: AstNodeVisitor<Unit>, ArgumentPusher {

  val classNode: ClassNode
  val methodNode: MethodNode
  val mv: MethodBytecodeWriter
  val typeResolver: JavaTypeResolver
  val lambdaHandler: LambdaHandler


  override fun visit(unaryMinus: UnaryMinus) {
   visit(MinusOperator(unaryMinus.token, IntConstantNode(unaryMinus.token, 0), unaryMinus.operand))
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
    mv.castIfNecessaryOrThrow(classNode.scope, operator, operator.getType(typeResolver), operator.trueExpression.getType(typeResolver))
    mv.jumpTo(endLabel)
    mv.visitLabel(falseLabel)
    operator.falseExpression.accept(this)
    mv.castIfNecessaryOrThrow(classNode.scope, operator, operator.getType(typeResolver), operator.falseExpression.getType(typeResolver))
    mv.visitLabel(endLabel)
  }


  override fun visit(switchNode: SwitchNode) {
    val switchExpressionType = switchNode.expressionNode.getType(typeResolver)
    visitConditionalBranchFlow(switchNode, MethodParameter(switchExpressionType, "it"), switchNode.expressionNode)
  }


  override fun visit(whenNode: WhenNode) {
    visitConditionalBranchFlow(whenNode)
  }

  private fun visitConditionalBranchFlow(switchNode: ConditionalBranchFlowNode<*>, itParameter: MethodParameter? = null, itArgument: ExpressionNode? = null) {
    if (switchNode.branches.isEmpty()) {
      throw MarcelSemanticException(switchNode.token, "Switch must have at least one branch")
    }

    val switchType = switchNode.getType(typeResolver)
    if (switchNode.elseStatement == null && switchType.primitive && switchType.type != JavaType.void) {
      throw MarcelSemanticException(switchNode.token, "Need to cover all cases (an else branch) for  switch returning primitives as they cannot be null")
    }

    val elseStatement = switchNode.elseStatement ?: ExpressionStatementNode(switchNode.token, NullValueNode(switchNode.token, switchType))

    // this part is to handle local variables referenced in the switch
    val currentScope = switchNode.scope
    // sorted set by insertion order
    val referencedLocalVariables = LinkedHashSet<LocalVariable>()
    (switchNode.branches + elseStatement).forEach { branchNode ->
      branchNode.forEachNode {
        if (it is ReferenceExpression) {
          val variable = currentScope.findLocalVariable(it.name)
          if (variable != null && it.name != "it") referencedLocalVariables.add(variable)
        }
      }
    }

    val referencedParameters = referencedLocalVariables.map {
      MethodParameter(it.type, it.name, true)
    }

    val parameters = if (itParameter != null) listOf(itParameter) + referencedParameters
    else referencedParameters

    val switchMethodName = "__when_" + ((switchNode.scope as? MethodScope)?.methodName ?: switchNode.scope.classType.simpleName) +
        classNode.methods.size
    val switchMethodScope = MethodScope(classNode.scope, switchMethodName, parameters, switchType, staticContext = false)

    // set scope
    switchNode.branches.forEach { it.setTreeScope(switchMethodScope) }
    elseStatement.setTreeScope(switchMethodScope)

    val branches = switchNode.branches
    if (switchType != JavaType.void) {
      branches.forEach { it.statementNode = returningLastStatement(switchMethodScope, it.statementNode) }
    }

    // marcel switch is just an if/elsif
    val rootIf = branches.first().toIf()
    var currentIf = rootIf
    for (i in 1..branches.lastIndex) {
      val branch = branches[i]
      val newIfBranch = branch.toIf()
      currentIf.falseStatementNode = newIfBranch
      currentIf = newIfBranch
    }
    currentIf.falseStatementNode = if (switchType != JavaType.void) returningLastStatement(switchMethodScope, elseStatement)
     else elseStatement

    val methodNode = MethodNode(Opcodes.ACC_PUBLIC, classNode.type,
      switchMethodName, FunctionBlockNode(switchNode.token, switchMethodScope, mutableListOf(rootIf)),
      parameters.map { MethodParameterNode(it) }.toMutableList(), switchType, switchMethodScope, false, emptyList()
    )
    methodNode.block.setTreeScope(switchMethodScope)

    classNode.addMethod(methodNode)
    typeResolver.defineMethod(classNode.type, methodNode)

    val referencedArguments = referencedLocalVariables.map { ReferenceExpression(switchNode.token, currentScope, it.name) }
    val callArguments =
      if (itArgument != null) (listOf(itArgument) + referencedArguments)
      else referencedArguments
    visit(SimpleFunctionCallNode(switchNode.token, currentScope, switchMethodName,
      callArguments.toMutableList(),
      ReferenceExpression.thisRef(currentScope), methodNode))
  }

  private fun returningLastStatement(methodScope: MethodScope, statementNode: StatementNode): StatementNode {
    return when (statementNode) {
      is BlockStatement -> {
        val lastStatement = statementNode.block.statements.lastOrNull()
        if (lastStatement == null) statementNode.block.addStatement(ReturnNode(statementNode.token, methodScope, NullValueNode(statementNode.token)))
        else if (lastStatement is ExpressionStatementNode) statementNode.block.statements.set(
          statementNode.block.statements.size - 1, ReturnNode(statementNode.token, methodScope, lastStatement.expression)
        ) else statementNode.block.addStatement(ReturnNode(statementNode.token, methodScope, NullValueNode(statementNode.token)))
        statementNode
      }
      is ExpressionStatementNode -> return ReturnNode(statementNode.token, methodScope, statementNode.expression)
      else -> BlockStatement(
          BlockNode(statementNode.token,
            // scope don't really matters here because it will be overriden in lambda
            methodScope,
            mutableListOf(
              statementNode, ReturnNode(statementNode.token, methodScope, NullValueNode(statementNode.token))
            )
          )
        )
    }
  }

  override fun visit(switchBranch: SwitchBranchNode) {
    throw RuntimeException("Compiler error. Shouldn't happen")
  }

  override fun visit(whenBranchNode: WhenBranchNode) {
    throw RuntimeException("Compiler error. Shouldn't happen")
  }

  override fun visit(ifStatementNode: IfStatementNode) {
    val optTruthyDeclarationNode = ifStatementNode.condition.innerExpression as? TruthyVariableDeclarationNode
    if (optTruthyDeclarationNode != null) {
      // declaring truthy variable (will and should only be used in trueStatement)
      visit(VariableDeclarationNode(ifStatementNode.token, optTruthyDeclarationNode.scope, optTruthyDeclarationNode.variableType, optTruthyDeclarationNode.name,
        optTruthyDeclarationNode.isFinal, optTruthyDeclarationNode.expression))
    }
    pushArgument(ifStatementNode.condition)
    val endLabel = Label()
    if (ifStatementNode.falseStatementNode == null) {
      mv.jumpIfEq(endLabel)
      ifStatementNode.trueStatementNode.accept(this)
      mv.visitLabel(endLabel)
      optTruthyDeclarationNode?.scope?.freeVariable(optTruthyDeclarationNode.name)
    } else {
      val falseStatementNode = ifStatementNode.falseStatementNode!!
      val falseLabel = Label()
      mv.jumpIfEq(falseLabel)
      ifStatementNode.trueStatementNode.accept(this)
      // this variable is only accessible in true statement, we don't need it after (especially for the else statement. it should
      //  be disposed before accepting it)
      optTruthyDeclarationNode?.scope?.freeVariable(optTruthyDeclarationNode.name)
      mv.jumpTo(endLabel)
      mv.visitLabel(falseLabel)
      falseStatementNode.accept(this)
      mv.visitLabel(endLabel)
    }
  }

  override fun visit(elvisOperator: ElvisOperator) {
    val scope = elvisOperator.scope
    val type = elvisOperator.getType(typeResolver)

    val tempVar = scope.addLocalVariable(type)

    visitWithoutPushing(VariableAssignmentNode(elvisOperator.token, scope, tempVar.name, elvisOperator.leftOperand))
    val leftOperandRef = ReferenceExpression(elvisOperator.token, scope, tempVar.name)
    visit(TernaryNode(elvisOperator.token,
      BooleanExpressionNode.of(elvisOperator.token, leftOperandRef),
      leftOperandRef, elvisOperator.rightOperand
    ))
    scope.freeVariable(tempVar.name)
  }

  override fun visit(fCall: ConstructorCallNode) {
    if (fCall.type.primitive) {
      throw MarcelSemanticException(fCall.token, "Cannot instantiate a primitive type")
    }
    mv.visitConstructorCall(fCall)
  }

  override fun visit(fCall: NamedParametersConstructorCallNode) {
    mv.visitNamedConstructorCall(fCall)
  }

  override fun visit(fCall: SuperConstructorCallNode) {
    if (!methodNode.isConstructor) {
      throw MarcelSemanticException(fCall.token, "Cannot call super constructor in a non constructor method")
    }
    if ((methodNode.block.statements.firstOrNull() as? ExpressionStatementNode)?.expression !== fCall) {
      throw MarcelSemanticException(fCall.token, "Super constructor call should be the first statement of a constructor")
    }
    mv.visitSuperConstructorCall(fCall)
  }

  override fun visit(operator: MulOperator) {
    arithmeticMarcelOperator(operator, JavaPrimitiveType::mulCode)
  }

  override fun visit(operator: DivOperator) {
    arithmeticMarcelOperator(operator, JavaPrimitiveType::divCode)
  }

  override fun visit(operator: MinusOperator) {
    arithmeticMarcelOperator(operator, JavaPrimitiveType::subCode)
  }

  override fun visit(operator: PlusOperator) {
    if (operator.leftOperand.getType(typeResolver) == JavaType.String || operator.rightOperand.getType(typeResolver) == JavaType.String) {
      StringNode.of(operator.token, listOf(operator.leftOperand, operator.rightOperand)).accept(this)
    } else {
      arithmeticMarcelOperator(operator, JavaPrimitiveType::addCode)
    }
  }

  override fun visit(operator: PowOperator) {
    arithmeticMarcelOperator(operator) {
      throw MarcelSemanticException("Operator pow is not handled yet")
    }
  }

  override fun visit(leftShiftOperator: LeftShiftOperator) {
    marcelOperator(leftShiftOperator)
  }

  override fun visit(rightShiftOperator: RightShiftOperator) {
    marcelOperator(rightShiftOperator)
  }

  fun arithmeticMarcelOperator(operator: BinaryOperatorNode, insCodeExtractor: (JavaPrimitiveType) -> Int): JavaType {
    val leftType = operator.leftOperand.getType(typeResolver)
    val rightType = operator.rightOperand.getType(typeResolver)
    if (leftType.isPrimitiveOrObjectPrimitive && rightType.isPrimitiveOrObjectPrimitive) {
      val type = operator.getType(typeResolver)
      pushArithmeticBinaryOperatorOperands(operator)
      insCodeExtractor.invoke(type.asPrimitiveType)
      mv.visitInsn(insCodeExtractor.invoke(type.asPrimitiveType))
      return type
    } else {
      return marcelOperator(operator)
    }
  }

  private fun pushArithmeticBinaryOperatorOperands(binaryOperatorNode: BinaryOperatorNode) {
    val type = binaryOperatorNode.getType(typeResolver)
    pushArgument(binaryOperatorNode.leftOperand)
    mv.castIfNecessaryOrThrow(methodNode.scope, binaryOperatorNode, type, binaryOperatorNode.leftOperand.getType(typeResolver))
    pushArgument(binaryOperatorNode.rightOperand)
    mv.castIfNecessaryOrThrow(methodNode.scope, binaryOperatorNode, type, binaryOperatorNode.rightOperand.getType(typeResolver))
  }

  fun marcelOperator(binaryOperatorNode: BinaryOperatorNode): JavaType {
    val type1 = binaryOperatorNode.leftOperand.getType(typeResolver)
    if (binaryOperatorNode.operatorMethodName == null) {
      val type2 = binaryOperatorNode.leftOperand.getType(typeResolver)
      throw MarcelSemanticException(binaryOperatorNode.token, "Doesn't handle this operator with types $type1 $type2")
    }
    val leftShiftMethod = typeResolver.findMethodOrThrow(type1, binaryOperatorNode.operatorMethodName!!, listOf(binaryOperatorNode.rightOperand.getType(typeResolver)))
    pushArgument(binaryOperatorNode.leftOperand)
    mv.invokeMethodWithArguments(binaryOperatorNode, methodNode.scope, leftShiftMethod, binaryOperatorNode.rightOperand)
    return leftShiftMethod.returnType
  }


  override fun visit(asNode: AsNode) {
    val expression = asNode.expressionNode
    if (expression is LiteralArrayNode) {
      val arrayType = if (asNode.type.isArray) asNode.type.asArrayType
      else if (asNode.type == JavaType.intList || asNode.type == JavaType.intSet) JavaType.intArray
      else if (asNode.type == JavaType.longList || asNode.type == JavaType.longSet) JavaType.longArray
      else if (asNode.type == JavaType.floatList || asNode.type == JavaType.floatSet) JavaType.floatArray
      else if (asNode.type == JavaType.doubleList || asNode.type == JavaType.doubleSet) JavaType.doubleArray
      else if (asNode.type == JavaType.charList || asNode.type == JavaType.characterSet) JavaType.charArray
      else if (asNode.type.raw() == List::class.javaType || asNode.type.raw() == Set::class.javaType) JavaType.objectArray
      else throw MarcelSemanticException(asNode.token, "Array cannot be converted into " + asNode.type)
      expression.type = arrayType
      // literal arrays can also be cast as collections (which will be handled in castIfNecessaryOrThrow
      if (expression.elements.isEmpty()) {
        visit(EmptyArrayNode(asNode.token, arrayType))
      } else {
        visit(expression)
      }
      mv.castIfNecessaryOrThrow(asNode.scope, asNode, asNode.type, arrayType)
    } else if (asNode.type == JavaType.boolean || asNode.type == JavaType.Boolean) {
      visit(BooleanExpressionNode.of(asNode.token, asNode.expressionNode))
      if (asNode.type == JavaType.Boolean) {
        mv.castIfNecessaryOrThrow(classNode.scope, asNode, JavaType.Boolean, JavaType.boolean)
      }
    } else {
      asNode.expressionNode.accept(this)
      mv.castIfNecessaryOrThrow(classNode.scope, asNode, asNode.type, asNode.expressionNode.getType(typeResolver))
    }
  }

  override fun visit(isOperator: IsOperator) {
    if (isOperator.leftOperand.getType(typeResolver).primitive || isOperator.rightOperand.getType(typeResolver).primitive) {
      throw MarcelSemanticException(isOperator.token, "Cannot apply '===' operator on primitive types")
    }
    pushBinaryOperatorOperands(isOperator)
    val l1 = Label()
    mv.jump(Opcodes.IF_ACMPEQ, l1) // Jump if the two object references are equal

    mv.visitInsn(Opcodes.ICONST_0) // Load false on the stack
    val l2 = Label()
    mv.jumpTo(l2) // Jump to the end of the method
    mv.visitLabel(l1)
    mv.visitInsn(Opcodes.ICONST_1) // Load true on the stack
    mv.visitLabel(l2)
  }
  override fun visit(isNotOperator: IsNotOperator) {
    if (isNotOperator.leftOperand.getType(typeResolver).primitive || isNotOperator.rightOperand.getType(typeResolver).primitive) {
      throw MarcelSemanticException(isNotOperator.token, "Cannot apply '!==' operator on primitive types")
    }

    pushBinaryOperatorOperands(isNotOperator)
    val l1 = Label()
    mv.jump(Opcodes.IF_ACMPNE, l1) // Jump if the two object references are equal

    mv.visitInsn(Opcodes.ICONST_0) // Load false on the stack
    val l2 = Label()
    mv.jumpTo(l2) // Jump to the end of the method
    mv.visitLabel(l1)
    mv.visitInsn(Opcodes.ICONST_1) // Load true on the stack
    mv.visitLabel(l2)
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
      mv.castIfNecessaryOrThrow(classNode.scope, comparisonOperatorNode, JavaType.Object, leftOperand.getType(typeResolver))
      pushArgument(rightOperand)
      mv.castIfNecessaryOrThrow(classNode.scope, comparisonOperatorNode, JavaType.Object, rightOperand.getType(typeResolver))
      if ((leftOperand is NullValueNode || rightOperand is NullValueNode)) {
        objectcomparison = true
        if (operator != ComparisonOperator.EQUAL && operator != ComparisonOperator.NOT_EQUAL) {
          throw MarcelSemanticException(comparisonOperatorNode.token, "Cannot compare null value with ${operator.symbolString} operator")
        }
      } else {
        when (operator) {
          ComparisonOperator.EQUAL, ComparisonOperator.NOT_EQUAL -> {
            mv.invokeMethod(comparisonOperatorNode, classNode.scope, BytecodeHelper::class.java.getDeclaredMethod("objectsEqual", JavaType.Object.realClazz, JavaType.Object.realClazz))
            if (operator == ComparisonOperator.NOT_EQUAL) mv.not()
            return // the above method returns a boolean
          }
          else -> {
            val method = typeResolver.findMethodOrThrow(leftOperand.getType(typeResolver), "compareTo", listOf(rightOperand.getType(typeResolver)))
            if (method.returnType != JavaType.int) throw MarcelSemanticException(comparisonOperatorNode.token, "compareTo method should return an int in order to be used in comparator")
            mv.invokeMethod(comparisonOperatorNode, classNode.scope, method)
            mv.pushConstant(0) // pushing 0 because we're comparing two numbers below
          }
        }
      }
    } else if (leftOperand.getType(typeResolver) !in ComparisonOperator.INT_LIKE_COMPARABLE_TYPES || rightOperand.getType(typeResolver) !in ComparisonOperator.INT_LIKE_COMPARABLE_TYPES) {
      val otherType = if (leftOperand.getType(typeResolver) != JavaType.int) leftOperand.getType(typeResolver) else rightOperand.getType(typeResolver)
      pushArgument(leftOperand)
      mv.castIfNecessaryOrThrow(classNode.scope, comparisonOperatorNode, otherType, leftOperand.getType(typeResolver))
      pushArgument(rightOperand)
      mv.castIfNecessaryOrThrow(classNode.scope, comparisonOperatorNode, otherType, rightOperand.getType(typeResolver))
      when (otherType) {
        JavaType.double -> mv.visitInsn(Opcodes.DCMPL)
        JavaType.float -> mv.visitInsn(Opcodes.FCMPL)
        JavaType.long -> mv.visitInsn(Opcodes.LCMP)
        else -> throw UnsupportedOperationException("Doesn't handle comparison of primitive type $otherType")
      }
      mv.pushConstant(0) // pushing 0 because we're comparing two numbers below
    } else {
      pushBinaryOperatorOperands(comparisonOperatorNode)
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

  override fun visit(findOperator: FindOperator) {
    if (!CharSequence::class.java.javaType.isAssignableFrom(findOperator.leftOperand.getType(typeResolver))) {
      throw MarcelSemanticException(findOperator.token, "Left operand of find operator should be a string")
    }
    if (!Pattern::class.java.javaType.isAssignableFrom(findOperator.rightOperand.getType(typeResolver))) {
      throw MarcelSemanticException(findOperator.token, "Right operand of find operator should be a Pattern")
    }
    pushArgument(findOperator.rightOperand)
    mv.invokeMethodWithArguments(findOperator, classNode.scope, Pattern::class.java.getMethod("matcher", CharSequence::class.java), findOperator.leftOperand)
  }

  override fun visit(accessOperator: InvokeAccessOperator) {
    val access = accessOperator.rightOperand

    if (accessOperator.nullSafe) {
      val scope = access.scope

      // need a local variable to avoid evaluating twice
      val tempVar = scope.addLocalVariable(accessOperator.leftOperand.getType(typeResolver))
      visitWithoutPushing(VariableAssignmentNode(accessOperator.token, scope, tempVar.name, accessOperator.leftOperand))
      val tempRef = ReferenceExpression(accessOperator.token, scope, tempVar.name)

      visit(TernaryNode(accessOperator.token,
        BooleanExpressionNode.of(accessOperator.token, ComparisonOperatorNode(accessOperator.token, ComparisonOperator.NOT_EQUAL, tempRef, NullValueNode(accessOperator.token))),
        // using a new function call because we need to use the tempRef instead of the actual leftOperand
        SimpleFunctionCallNode(accessOperator.token, access.scope, access.name, access.getArguments(typeResolver), access.getMethod(typeResolver)).apply {
          methodOwnerType = tempRef
        }
        , NullValueNode(accessOperator.token)
      ))
      scope.freeVariable(tempVar.name)
    } else {
      access.methodOwnerType = accessOperator.leftOperand
      access.accept(this)
    }
  }

  override fun visit(getFieldAccessOperator: GetFieldAccessOperator) {
    val field = typeResolver.findFieldOrThrow(getFieldAccessOperator.leftOperand.getType(typeResolver), getFieldAccessOperator.rightOperand.name)
    if (field.isStatic) {
      mv.getField(getFieldAccessOperator, getFieldAccessOperator.scope, field)
      return
    }
    if (getFieldAccessOperator.nullSafe) {
      val scope = getFieldAccessOperator.scope

      // need a local variable to avoid evaluating twice
      val tempVar = scope.addLocalVariable(getFieldAccessOperator.leftOperand.getType(typeResolver))
      visitWithoutPushing(VariableAssignmentNode(getFieldAccessOperator.token, scope, tempVar.name, getFieldAccessOperator.leftOperand))
      val tempRef = ReferenceExpression(getFieldAccessOperator.token, scope, tempVar.name)

      visit(TernaryNode(getFieldAccessOperator.token,
        BooleanExpressionNode.of(getFieldAccessOperator.token, ComparisonOperatorNode(getFieldAccessOperator.token, ComparisonOperator.NOT_EQUAL, tempRef,
          NullValueNode(getFieldAccessOperator.token))),
        // using a new GetFieldAccessOperator because we need to use the tempRef instead of the actual leftOperand
        GetFieldAccessOperator(getFieldAccessOperator.token, tempRef, getFieldAccessOperator.rightOperand, false)
        , NullValueNode(getFieldAccessOperator.token)
      ))
      scope.freeVariable(tempVar.name)
    } else {
      pushArgument(getFieldAccessOperator.leftOperand)
      mv.getField(getFieldAccessOperator, getFieldAccessOperator.scope, field)
    }
  }

  private fun pushBinaryOperatorOperands(binaryOperatorNode: BinaryOperatorNode) {
    pushArgument(binaryOperatorNode.leftOperand)
    pushArgument(binaryOperatorNode.rightOperand)
  }

  override fun visit(fCall: FunctionCallNode) {
    val method = fCall.getMethod(typeResolver)
    val methodOwner = fCall.methodOwnerType
    if (!method.isInline) {
      if (!method.isStatic) {
        if (methodOwner is ExpressionNode) {
          pushArgument(methodOwner) // for instance method, we need to push owner
        } else {
          pushArgument(ReferenceExpression.thisRef(fCall.scope))
        }
      }
      mv.invokeMethodWithArguments(fCall, classNode.scope, method, fCall.getArguments(typeResolver))
    } else {
      // this probably doesn't work anymore, but hey, let's keep it for when I'll decide whether to implement this feature or not
      val inlineMethod = method as MethodNode
      val innerScope = InnerScope(
        fCall.scope as? MethodScope ?: throw MarcelSemanticException(fCall.token, "Can only call inline functions in a method"))
      val inlineBlock = inlineMethod.block.asSimpleBlock(inlineMethod.block.token, innerScope)
      inlineBlock.setTreeScope(innerScope)
      // initializing arguments
      if (fCall.getArguments(typeResolver).size != inlineMethod.parameters.size) {
        throw MarcelSemanticException(fCall.token, "Invalid number of arguments for method ${method.name}")
      }
      val variables = method.parameters.map { innerScope.addLocalVariable(it.type, it.name) }
      for (i in variables.indices) {
        visit(VariableAssignmentNode(fCall.token, innerScope, variables[i].name, fCall.getArguments(typeResolver)[i]))
      }
      visit(inlineBlock)
    }
  }

  override fun visit(lambdaNode: LambdaNode) {
    val constructorCall = lambdaHandler.defineLambda(lambdaNode)
    visit(constructorCall)
  }

  override fun visit(assignmentNode: VariableAssignmentNode) {
    if (classNode.isScript && !assignmentNode.scope.hasVariable(assignmentNode.name)) {
      // we define the field dynamically for scripts
      typeResolver.defineField(classNode.type, BoundField(assignmentNode.expression.getType(typeResolver),
              assignmentNode.name, classNode.type))
    }

    val variable = assignmentNode.scope.findVariableOrThrow(assignmentNode.name)
    // needed to smart cast literal arrays into lists
    val variableAssignmentNode = if (assignmentNode.expression is LiteralArrayNode) VariableAssignmentNode(assignmentNode.token, assignmentNode.scope, assignmentNode.name,
            AsNode(assignmentNode.token, assignmentNode.scope, variable.type, assignmentNode.expression))
    else assignmentNode
    if (variable is MarcelField && !variable.isStatic) {
      if (variable.owner.isAssignableFrom(variableAssignmentNode.scope.classType)) {
        pushArgument(ReferenceExpression.thisRef(variableAssignmentNode.scope))
      } else {
        throw RuntimeException("Compiler error. Shouldn't push class field of not current class with this method")
      }
    }

    pushAssignmentExpression(variable, variableAssignmentNode.expression)
    mv.storeInVariable(variableAssignmentNode, variableAssignmentNode.scope, variable)
  }

  private fun pushAssignmentExpression(variable: Variable, expression: ExpressionNode) {
    val variableType = variable.type
    guessExpressionTypeIfNeeded(variableType, expression)
    pushArgument(expression)
    mv.castIfNecessaryOrThrow(classNode.scope, expression, variable.type, expression.getType(typeResolver))
  }

  private fun guessExpressionTypeIfNeeded(variableType: JavaType, expression: ExpressionNode) {
    if (expression is LiteralArrayNode && expression.elements.isEmpty()) {
      val elementsType = if (variableType is JavaArrayType) variableType.elementsType
      else if (JavaType.intList.isAssignableFrom(variableType) || JavaType.intSet.isAssignableFrom(variableType)) JavaType.int
      else if (JavaType.longList.isAssignableFrom(variableType) || JavaType.longSet.isAssignableFrom(variableType)) JavaType.long
      else if (JavaType.floatList.isAssignableFrom(variableType) || JavaType.floatSet.isAssignableFrom(variableType)) JavaType.float
      else if (JavaType.doubleList.isAssignableFrom(variableType) || JavaType.doubleSet.isAssignableFrom(variableType)) JavaType.double
      else if (JavaType.charList.isAssignableFrom(variableType) || JavaType.characterSet.isAssignableFrom(variableType)) JavaType.char
      else if (JavaType.of(Collection::class.java).isAssignableFrom(variableType) && variableType.genericTypes.isNotEmpty()) variableType.genericTypes.first()
      else throw MarcelSemanticException(expression.token, "Couldn't guess type of empty array. You can explicitly specify your wanted type with the 'as' keyword (e.g. '[] as int[]')")
      expression.type = JavaType.arrayType(elementsType)
    } else if (variableType.isInterface && expression is LambdaNode) {
      expression.interfaceType = variableType
    }
  }

  override fun visit(assignmentNode: FieldAssignmentNode) {
    val fieldVariable = typeResolver.findFieldOrThrow(
            assignmentNode.fieldNode.leftOperand.getType(typeResolver),
            assignmentNode.fieldNode.rightOperand.name
    )
    // needed to smart cast literal arrays into lists
    val fieldAssignmentNode = if (assignmentNode.expression is LiteralArrayNode) FieldAssignmentNode(assignmentNode.token,
            assignmentNode.scope, assignmentNode.fieldNode, AsNode(assignmentNode.token, assignmentNode.scope, fieldVariable.type, assignmentNode.expression))
    else assignmentNode

    if (!fieldVariable.isStatic) {
      pushArgument(fieldAssignmentNode.fieldNode.leftOperand)
    }
    pushAssignmentExpression(fieldVariable, fieldAssignmentNode.expression)
    mv.storeInVariable(fieldAssignmentNode, fieldAssignmentNode.scope, fieldVariable)
  }

  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode) {
    val indexedReference = indexedVariableAssignmentNode.indexedReference
    mv.storeInVariablePutAt(indexedVariableAssignmentNode,
      indexedReference.scope, indexedReference.variable,
      indexedReference.indexArguments, indexedVariableAssignmentNode.expression
    )
  }
  override fun visit(indexedReferenceExpression: IndexedReferenceExpression) {
    if (indexedReferenceExpression.isSafeIndex) {
      // TODO document safe index acceess if not already done
      val funcCall = SimpleFunctionCallNode(indexedReferenceExpression.token, indexedReferenceExpression.scope, "getAtSafe",
        indexedReferenceExpression.indexArguments.toMutableList(),
        ReferenceExpression(indexedReferenceExpression.token, indexedReferenceExpression.scope, indexedReferenceExpression.name)
        )
       visit(funcCall)
      mv.castIfNecessaryOrThrow(classNode.scope, indexedReferenceExpression, indexedReferenceExpression.getType(typeResolver), funcCall.getType(typeResolver))
    } else {
      mv.pushVariableGetAt(indexedReferenceExpression, indexedReferenceExpression.scope, indexedReferenceExpression.variable,
        indexedReferenceExpression.indexArguments)
    }
  }

  override fun visit(voidExpression: VoidExpression) {
    // do nothing, it's void
  }
}

/**
 * Generates expression bytecode but don't push them to the stack. (Useful for statement expressions)
 */
class InstructionGenerator(
  override val classNode: ClassNode,
  override val methodNode: MethodNode,
  override val typeResolver: JavaTypeResolver,
  methodVisitor: MethodVisitor
):
  IInstructionGenerator {

  override val mv = MethodBytecodeWriter(methodVisitor, typeResolver)
  override val lambdaHandler = LambdaHandler(classNode, typeResolver)

  private val pushingInstructionGenerator = PushingInstructionGenerator(classNode, methodNode, typeResolver, mv, lambdaHandler)
  init {
    mv.argumentPusher = this
    pushingInstructionGenerator.mv.argumentPusher = pushingInstructionGenerator
  }
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

  override fun visit(tryCatchNode: TryCatchNode) {
    // TODO seems to work but compiled bytecode looks weird
    val tryStart = Label()
    val tryEnd = Label()
    val endLabel = Label()
    val catchesWithLabel = tryCatchNode.catchNodes.map {
      // need one label for each catch block
      it to Label()
    }
    val finallyWithLabel = if (tryCatchNode.finallyBlock != null) tryCatchNode.finallyBlock!! to Label() else null
    catchesWithLabel.forEach { c ->
      c.first.exceptionTypes.forEach { exceptionType ->
        if (!Throwable::class.javaType.isAssignableFrom(exceptionType)) {
          throw MarcelSemanticException("Can only catch throwable")
        }
        mv.tryCatchBlock(tryStart, tryEnd, c.second, exceptionType)
      }
    }
    if (finallyWithLabel != null) {
      mv.tryFinallyBlock(tryStart, tryEnd, finallyWithLabel.second)
    }

    mv.visitLabel(tryStart)
    tryCatchNode.tryStatementNode.accept(this)
    mv.visitLabel(tryEnd)
    finallyWithLabel?.first?.statementNode?.accept(this)

    mv.jumpTo(endLabel)

    catchesWithLabel.forEach { c ->
      val excVar = c.first.scope.addLocalVariable(JavaType.commonType(c.first.exceptionTypes), c.first.exceptionVarName)
      mv.catchBlock(c.second, excVar.index)
      c.first.statementNode.accept(this)
      finallyWithLabel?.first?.statementNode?.accept(this)
      mv.jumpTo(endLabel)
    }
    if (finallyWithLabel != null) {
      val excVar = finallyWithLabel.first.scope.addLocalVariable(Throwable::class.javaType)
      mv.catchBlock(finallyWithLabel.second, excVar.index)
      finallyWithLabel.first.statementNode.accept(this)
      mv.pushVariable(tryCatchNode, finallyWithLabel.first.scope, excVar)
      mv.visitInsn(Opcodes.ATHROW)
      mv.jumpTo(endLabel)
    }

    mv.visitLabel(endLabel)
  }
  override fun visit(forInStatement: ForInStatement) {
    val expression = forInStatement.inExpression
    val expressionType = expression.getType(typeResolver)

    // initialization
    val scope = forInStatement.scope
    scope.addLocalVariable(forInStatement.variableType, forInStatement.variableName)

    // creating iterator
    val iteratorExpression = if (Iterable::class.javaType.isAssignableFrom(expressionType)) SimpleFunctionCallNode(forInStatement.token, scope, "iterator", mutableListOf(), expression)
    else if (Iterator::class.javaType.isAssignableFrom(expressionType)) expression
    else if (CharSequence::class.javaType.isAssignableFrom(expressionType)) ConstructorCallNode(forInStatement.token, scope, CharSequenceIterator::class.java.javaType,
      mutableListOf(expression))
    else throw MarcelSemanticException(forInStatement.token, "Doesn't handle iterating on $expressionType")
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
    visit(VariableAssignmentNode(forInStatement.token, scope, iteratorVariable.name, iteratorExpression))

    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition
    val iteratorVarReference = ReferenceExpression(forInStatement.token, scope, iteratorVariable.name)
    pushArgument(iteratorVarReference)
    mv.invokeMethod(forInStatement, classNode.scope, IntIterator::class.java.getMethod("hasNext"))

    val loopEnd = Label()
    mv.jumpIfEq(loopEnd)

    // loop body
    visit(VariableAssignmentNode(forInStatement.token, scope, forInStatement.variableName, SimpleFunctionCallNode(forInStatement.token, scope, methodName, mutableListOf(), iteratorVarReference)))
    loopBody(forInStatement.body, loopStart, loopEnd)
    mv.jumpTo(loopStart)

    // loop end
    mv.visitLabel(loopEnd)

    if (Closeable::class.javaType.isAssignableFrom(iteratorExpressionType)) {
      // TODO would need to be in a finally block (which means the loop should be in a try block)
      pushArgument(iteratorVarReference)
      mv.invokeMethod(forInStatement, classNode.scope, Closeable::class.java.getMethod("close"))
    }
    scope.freeVariable(iteratorVariable.name)
  }
  private fun loopBody(body: BlockNode, continueLabel: Label, breakLabel: Label) {
    val scope = body.scope as? InnerScope ?: throw RuntimeException("Compiler design bug")
    scope.continueLabel = continueLabel
    scope.breakLabel = breakLabel
    body.accept(this)
  }

  override fun visit(breakLoopNode: BreakLoopNode) {
    val label = breakLoopNode.scope.breakLabel ?: throw MarcelSemanticException(breakLoopNode.token, "Cannot use break statement outside of a loop")
    mv.jumpTo(label)
  }

  override fun visit(continueLoopNode: ContinueLoopNode) {
    val label = continueLoopNode.scope.continueLabel ?: throw MarcelSemanticException(continueLoopNode.token, "Cannot use continue statement outside of a loop")
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

  override fun visit(shortConstantNode: ShortConstantNode) {
    // don't need to write constants
  }

  override fun visit(byteConstantNode: ByteConstantNode) {
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

  override fun visit(charNode: CharConstantNode) {
    // don't need to write constants
  }
  override fun visit(booleanConstantNode: BooleanConstantNode) {
    // don't need to write constants
  }
  override fun visit(stringConstantNode: StringConstantNode) {
    // don't need to write constants
  }

  override fun visit(thisReference: ThisReference) {
    // don't need to write this if it isn't used
  }


  override fun visit(patternValueNode: LiteralPatternNode) {
    // don't need to write this if it isn't used
  }

  override fun visit(superReference: SuperReference) {
    // don't need to write super if it isn't used
  }
  override fun visit(fCall: ConstructorCallNode) {
    super.visit(fCall)
    mv.popStack() // don't really know if it's necessary
  }

  override fun visit(fCall: NamedParametersConstructorCallNode) {
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
      visit(VariableAssignmentNode(incrNode.token, ref.scope, ref.name, PlusOperator(incrNode.token, ref, IntConstantNode(incrNode.token, incrNode.amount))))
    }
  }

  override fun visit(nullValueNode: NullValueNode) {
    // no need to push anything
  }

  override fun visit(classExpressionNode: ClassExpressionNode) {
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
        visit(ReturnNode(blockNode.token, blockNode.scope, lastStatement.expression))
      } else if (!blockNode.scope.returnType.primitive) {
        lastStatement?.accept(this)
        // just return null
        mv.pushNull()
        mv.returnCode(blockNode.scope.returnType.returnCode)
      } else if (lastStatement == null || !lastStatement.allBranchesReturn()) {
        throw MarcelSemanticException(blockNode.token, "Function returning primitive types must explicitly return an expression as the last statement." +
            "You must explicitly return an expression, a switch or a when.")
      } else {
        lastStatement.accept(this)
        mv.castIfNecessaryOrThrow(classNode.scope, blockNode, blockNode.scope.returnType, lastStatement.getType(typeResolver))
        mv.returnCode(blockNode.scope.returnType.returnCode)
      }
    }
  }

  override fun visit(operator: MulOperator) {
    super.visit(operator)
    mv.popStack()
  }
  override fun visit(operator: DivOperator) {
    super.visit(operator)
    mv.popStack()
  }

  override fun visit(operator: MinusOperator) {
    super.visit(operator)
    mv.popStack()
  }

  override fun visit(operator: PlusOperator) {
    super.visit(operator)
    mv.popStack()
  }

  override fun visit(rightShiftOperator: RightShiftOperator) {
    super.visit(rightShiftOperator)
    mv.popStack()
  }

  override fun visit(leftShiftOperator: LeftShiftOperator) {
    super.visit(leftShiftOperator)
    mv.popStack()
  }

  override fun visit(operator: PowOperator) {
    super.visit(operator)
    mv.popStack()
  }

  override fun visit(comparisonOperatorNode: ComparisonOperatorNode) {
    super.visit(comparisonOperatorNode)
    mv.popStack()
  }

  override fun visit(isOperator: IsOperator) {
    super.visit(isOperator)
    mv.popStack()
  }
  override fun visit(isNotOperator: IsNotOperator) {
    super.visit(isNotOperator)
    mv.popStack()
  }

  override fun visit(andOperator: AndOperator) {
    super.visit(andOperator)
    mv.popStack()
  }

  override fun visit(findOperator: FindOperator) {
    super.visit(findOperator)
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
      throw MarcelSemanticException(multiVariableDeclarationNode.token, "Multi variable declarations must use an array or a list as the expression")
    }
    val tempVar = scope.addLocalVariable(expressionType)
    // assign expression to variable
    visit(VariableAssignmentNode(multiVariableDeclarationNode.token, scope, tempVar.name, multiVariableDeclarationNode.expression))
    // then process each variable declarations
    for (i in multiVariableDeclarationNode.declarations.indices) {
      val declaration = multiVariableDeclarationNode.declarations[i] ?: continue
      visit(VariableDeclarationNode(multiVariableDeclarationNode.token, scope, declaration.first, declaration.second, false,
        IndexedReferenceExpression(multiVariableDeclarationNode.token, scope, tempVar.name, listOf(IntConstantNode(multiVariableDeclarationNode.token, i)), false)))
    }
    scope.freeVariable(tempVar.name)
  }

  override fun visit(truthyVariableDeclarationNode: TruthyVariableDeclarationNode) {
    // variable should have been declared in the visit(ifStatementNode)
    visit(
      VariableAssignmentNode(truthyVariableDeclarationNode.token, truthyVariableDeclarationNode.scope, truthyVariableDeclarationNode.name, truthyVariableDeclarationNode.expression)
    )
  }

  override fun visit(blockNode: BlockNode) {
    for (statement in blockNode.statements) {
      statement.accept(this)
    }
  }

  override fun visit(returnNode: ReturnNode) {
    if (returnNode.scope.returnType == JavaType.void && returnNode.expression !is VoidExpression) {
      throw MarcelSemanticException(returnNode.token, "Cannot return an expression in a void function")
    }
    pushArgument(returnNode.expression)
    mv.castIfNecessaryOrThrow(classNode.scope, returnNode, returnNode.scope.returnType, returnNode.expression.getType(typeResolver))
    mv.returnCode(returnNode.scope.returnType.returnCode)
  }

  override fun visitWithoutPushing(astNode: AstInstructionNode) {
    astNode.accept(this)
  }
}

private class PushingInstructionGenerator(
  override val classNode: ClassNode,
  override val methodNode: MethodNode,
  override val typeResolver: JavaTypeResolver,
  override val mv: MethodBytecodeWriter,
  override val lambdaHandler: LambdaHandler,
  ): IInstructionGenerator {
  lateinit var instructionGenerator: InstructionGenerator


  override fun visit(forStatement: ForStatement) {
    instructionGenerator.visit(forStatement)
  }

  override fun visit(tryCatchNode: TryCatchNode) {
    instructionGenerator.visit(tryCatchNode)
  }

  override fun visit(forInStatement: ForInStatement) {
    instructionGenerator.visit(forInStatement)
  }

  override fun visit(literalListNode: LiteralArrayNode) {
    mv.newArray(classNode.scope, literalListNode, literalListNode.getType(typeResolver).asArrayType, literalListNode.elements)
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
          throw MarcelSemanticException(literalMapNode.token, "Doesn't handle maps of type ${literalMapNode.getType(typeResolver)}")
        }
      }
    }
    mv.invokeMethod(literalMapNode, classNode.scope, BytecodeHelper::class.java.getDeclaredMethod(methodName))
    val keysType = literalMapNode.getKeysType(typeResolver)
    val putMethodKeysType = if (keysType.primitive) keysType else JavaType.Object
    val rawMapType = literalMapNode.getType(typeResolver).raw()

    for (entry in literalMapNode.entries) {
      mv.dup()
      pushArgument(entry.first)
      if (objectKeys) {
        mv.castIfNecessaryOrThrow(classNode.scope, literalMapNode, JavaType.Object, entry.first.getType(typeResolver))
      } else {
        mv.castIfNecessaryOrThrow(classNode.scope, literalMapNode, putMethodKeysType, entry.first.getType(typeResolver))
      }
      pushArgument(entry.second)
      mv.castIfNecessaryOrThrow(classNode.scope, literalMapNode, JavaType.Object, entry.second.getType(typeResolver))
      mv.invokeMethod(literalMapNode, classNode.scope, typeResolver.findMethodOrThrow(rawMapType,
        "put",
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
    val fromType = rangeNode.from.getType(typeResolver)
    val toType = rangeNode.to.getType(typeResolver)
    val method =
      if (fromType == JavaType.long || fromType == JavaType.Long
        || toType == JavaType.long || toType == JavaType.Long) ReflectJavaMethod(LongRanges::class.java.getMethod(methodName, Long::class.java, Long::class.java))
      else ReflectJavaMethod(IntRanges::class.java.getMethod(methodName, Int::class.java, Int::class.java))
    mv.invokeMethodWithArguments(rangeNode, classNode.scope, method, rangeNode.from, rangeNode.to)
  }

  override fun visit(notNode: NotNode) {
    when (notNode.operand.getType(typeResolver)) {
      JavaType.Boolean -> mv.invokeMethodWithArguments(notNode, classNode.scope, typeResolver.findMethodOrThrow(JavaType.Boolean, "booleanValue", emptyList()), notNode.operand)
      JavaType.boolean -> notNode.operand.accept(this)
      else -> visit(BooleanExpressionNode.of(notNode.token, notNode.operand))
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
    val innerType = booleanExpression.innerExpression.getType(typeResolver)
    if (booleanExpression.innerExpression is NullValueNode) {
      visit(BooleanConstantNode(booleanExpression.token, false))
    } else if (innerType == JavaType.boolean
      || innerType == JavaType.Boolean) {
      booleanExpression.innerExpression.accept(this)
      mv.castIfNecessaryOrThrow(classNode.scope, booleanExpression, JavaType.boolean, innerType)
    } else if (innerType.primitive) {
      // according to marcel truth, all primitive are truthy
      booleanExpression.innerExpression.accept(instructionGenerator)
      visit(BooleanConstantNode(booleanExpression.token, true))
    } else if (Matcher::class.javaType.isAssignableFrom(innerType)) {
      pushArgument(booleanExpression.innerExpression)
      mv.invokeMethod(booleanExpression, classNode.scope, Matcher::class.java.getMethod("find"))
    } else {
      val classTruthyMethod = typeResolver.findMethod(innerType, "isTruthy", emptyList())
      if (classTruthyMethod != null) {
        pushArgument(booleanExpression.innerExpression)
        mv.invokeMethod(booleanExpression.innerExpression, classNode.scope, classTruthyMethod)
      } else {
        // this is a static method. No need to push owner
        val marcelTruthyMethod = typeResolver.findMethodOrThrow(MarcelTruth::class.javaType, "truthy", listOf(innerType))
        mv.invokeMethodWithArguments(booleanExpression, classNode.scope, marcelTruthyMethod, booleanExpression.innerExpression)
      }
    }
  }

  override fun visit(toStringNode: ToStringNode) {
    val expr = toStringNode.expressionNode
    if (expr.getType(typeResolver) == JavaType.String) {
      expr.accept(this)
    } else {
      val argumentType = expr.getType(typeResolver)
      if (argumentType.primitive) {
        mv.invokeMethodWithArguments(toStringNode, classNode.scope, String::class.java.getDeclaredMethod("valueOf", argumentType.realClazz), expr)
      } else {
        mv.invokeMethodWithArguments(toStringNode, classNode.scope, String::class.java.getDeclaredMethod("valueOf", JavaType.Object.realClazz), expr)
      }
    }
  }
  override fun visit(stringNode: StringNode) {
    if (stringNode.parts.isEmpty()) {
      // empty string
      StringConstantNode(stringNode.token, "").accept(this)
      return
    } else if (stringNode.parts.size == 1) {
      ToStringNode.of(stringNode.token, stringNode.parts.first()).accept(this)
      return
    }
    // new StringBuilder() can just provide an empty new scope as we'll just use it to extract the method from StringBuilder which already exists in the JDK
    val type = StringBuilder::class.javaType
    visit(ConstructorCallNode(stringNode.token, Scope(typeResolver, type, false), type, mutableListOf()))
    for (part in stringNode.parts) {
      // chained calls
      val argumentType = part.getType(typeResolver)
      val method = ReflectJavaMethod(StringBuilder::class.java.getDeclaredMethod("append",
        if (argumentType.primitive) argumentType.realClazz else JavaType.Object.realClazz))
      mv.invokeMethodWithArguments(stringNode, classNode.scope, method, part)
    }
    mv.invokeMethod(stringNode, classNode.scope, StringBuilder::class.java.getDeclaredMethod("toString"))
  }

  override fun visit(integer: IntConstantNode) {
    mv.pushConstant(integer.value)
  }

  override fun visit(longConstantNode: LongConstantNode) {
    mv.pushConstant(longConstantNode.value)
  }

  override fun visit(shortConstantNode: ShortConstantNode) {
    mv.pushConstant(shortConstantNode.value)
  }

  override fun visit(byteConstantNode: ByteConstantNode) {
    mv.pushConstant(byteConstantNode.value)
  }
  override fun visit(floatConstantNode: FloatConstantNode) {
    mv.pushConstant(floatConstantNode.value)
  }

  override fun visit(doubleConstantNode: DoubleConstantNode) {
    mv.pushConstant(doubleConstantNode.value)
  }

  override fun visit(charNode: CharConstantNode) {
    val value = charNode.value
    if (value.length != 1) throw MarcelSemanticException(charNode.token, "Characters should be strings of exactly one char")
    mv.pushConstant(value[0])
  }
  override fun visit(nullValueNode: NullValueNode) {
    mv.pushNull()
  }

  override fun visit(superReference: SuperReference) {
    if (methodNode.isStatic) throw MarcelSemanticException(superReference.token, "Cannot reference 'super' in a static context")
    mv.pushThis() // super is actually this. The difference is in the class internalName supplied when performing ASM instructions
  }

  override fun visit(thisReference: ThisReference) {
    when {
      !methodNode.isStatic -> mv.pushThis()
      // for extension class
      classNode.isExtensionClass && thisReference.scope.hasVariable("self") -> visit(ReferenceExpression(thisReference.token, thisReference.scope, "self"))
      else -> throw MarcelSemanticException(thisReference.token, "Cannot reference 'this' in a static context")
    }
  }

  override fun visit(patternValueNode: LiteralPatternNode) {
    mv.pushConstant(patternValueNode.value)
    if (patternValueNode.flags.isNotEmpty()) {
      val flag = patternValueNode.flags.reduce { acc, i -> acc or i }
      mv.pushConstant(flag)
      mv.invokeMethod(patternValueNode, classNode.scope, Pattern::class.java.getMethod("compile", String::class.java, Int::class.java))
    } else {
      mv.invokeMethod(patternValueNode, classNode.scope, Pattern::class.java.getMethod("compile", String::class.java))
    }
  }
  override fun visit(incrNode: IncrNode) {
    if (incrNode.returnValueBefore) {
      mv.pushVariable(incrNode, incrNode.variableReference.scope, incrNode.variableReference.variable)
      instructionGenerator.visit(incrNode)
    } else {
      instructionGenerator.visit(incrNode)
      mv.pushVariable(incrNode, incrNode.variableReference.scope, incrNode.variableReference.variable)
    }
  }
  override fun visit(booleanConstantNode: BooleanConstantNode) {
    mv.pushConstant(booleanConstantNode.value)
  }
  override fun visit(referenceExpression: ReferenceExpression) {
    mv.pushVariable(referenceExpression, referenceExpression.scope, referenceExpression.variable)
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    super.visit(variableAssignmentNode)
    mv.pushVariable(variableAssignmentNode, variableAssignmentNode.scope, variableAssignmentNode.scope.findVariableOrThrow(variableAssignmentNode.name))
  }

  override fun visit(fieldAssignmentNode: FieldAssignmentNode) {
    super.visit(fieldAssignmentNode)
    val field = typeResolver.findFieldOrThrow(fieldAssignmentNode.fieldNode.leftOperand.getType(typeResolver), fieldAssignmentNode.fieldNode.rightOperand.name)
    if (!field.isStatic) {
      pushArgument(fieldAssignmentNode.fieldNode.leftOperand)
    }
    mv.pushVariable(fieldAssignmentNode, fieldAssignmentNode.scope, field)
  }

  override fun visit(indexedVariableAssignmentNode: IndexedVariableAssignmentNode) {
    super.visit(indexedVariableAssignmentNode)
    pushArgument(indexedVariableAssignmentNode.indexedReference)
  }
  override fun pushArgument(expr: ExpressionNode) {
    expr.accept(this)
  }

  override fun visit(returnNode: ReturnNode) {
    instructionGenerator.visit(returnNode)
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
      actualTruthyVariableDeclarationNode = TruthyVariableDeclarationNode(truthyVariableDeclarationNode.token,
        actualTruthyVariableDeclarationNode.scope, actualTruthyVariableDeclarationNode.variableType, actualTruthyVariableDeclarationNode.name,
        InvokeAccessOperator(truthyVariableDeclarationNode.token, actualTruthyVariableDeclarationNode.expression,
          SimpleFunctionCallNode(truthyVariableDeclarationNode.token, actualTruthyVariableDeclarationNode.scope, "orElse", mutableListOf(NullValueNode(truthyVariableDeclarationNode.token, actualTruthyVariableDeclarationNode.variableType)))
        , false)
      )
    }
    instructionGenerator.visit(actualTruthyVariableDeclarationNode)
    if (actualTruthyVariableDeclarationNode.variableType.primitive) {
      visit(BooleanConstantNode(truthyVariableDeclarationNode.token, true))
    } else {
      pushArgument(BooleanExpressionNode.of(truthyVariableDeclarationNode.token,
        ReferenceExpression(truthyVariableDeclarationNode.token, actualTruthyVariableDeclarationNode.scope, actualTruthyVariableDeclarationNode.name)
      ))
    }
  }
  override fun visit(blockNode: BlockNode) {
    instructionGenerator.visit(blockNode)
  }

  override fun visit(blockNode: FunctionBlockNode) {
    instructionGenerator.visit(blockNode)
  }

  override fun visit(classExpressionNode: ClassExpressionNode) {
    mv.pushClass(classExpressionNode.clazz)
  }

  override fun visitWithoutPushing(astNode: AstInstructionNode) {
    astNode.accept(instructionGenerator)
  }
}