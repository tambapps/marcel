package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.LambdaHandler
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.ast.MethodParameter
import com.tambapps.marcel.parser.ast.AstInstructionNode
import com.tambapps.marcel.parser.ast.AstNode
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
import com.tambapps.marcel.parser.scope.JavaField
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.scope.Variable
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaMethod
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
import kotlin.collections.HashMap

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


  override fun visit(node: UnaryMinus) {
   visit(MinusOperator(node.token, IntConstantNode(node.token, 0), node.operand))
  }

  override fun visit(node: UnaryPlus) {
    node.operand.accept(this)
  }

  override fun visit(node: TernaryNode) {
    pushArgument(node.boolExpression)
    val endLabel = Label()
    val falseLabel = Label()
    mv.jumpIfEq(falseLabel)
    node.trueExpression.accept(this)
    mv.castIfNecessaryOrThrow(classNode.scope, node, node.getType(typeResolver), node.trueExpression.getType(typeResolver))
    mv.jumpTo(endLabel)
    mv.visitLabel(falseLabel)
    node.falseExpression.accept(this)
    mv.castIfNecessaryOrThrow(classNode.scope, node, node.getType(typeResolver), node.falseExpression.getType(typeResolver))
    mv.visitLabel(endLabel)
  }


  override fun visit(node: SwitchNode) {
    val switchExpressionType = node.expressionNode.getType(typeResolver)
    visitConditionalBranchFlow(node, MethodParameter(switchExpressionType, "it"), node.expressionNode)
  }


  override fun visit(node: WhenNode) {
    visitConditionalBranchFlow(node)
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

    val methodNode = MethodNode(switchNode.token, Opcodes.ACC_PUBLIC, classNode.type,
      switchMethodName, FunctionBlockNode(switchNode.token, switchMethodScope, mutableListOf(rootIf)),
      parameters.map { MethodParameterNode(switchNode.token, it) }.toMutableList(), switchType, switchMethodScope, false, emptyList()
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

  override fun visit(node: SwitchBranchNode) {
    throw RuntimeException("Compiler error. Shouldn't happen")
  }

  override fun visit(node: WhenBranchNode) {
    throw RuntimeException("Compiler error. Shouldn't happen")
  }

  override fun visit(node: IfStatementNode) {
    val optTruthyDeclarationNode = node.condition.innerExpression as? TruthyVariableDeclarationNode
    pushArgument(node.condition)
    val endLabel = Label()
    if (node.falseStatementNode == null) {
      mv.jumpIfEq(endLabel)
      node.trueStatementNode.accept(this)
      mv.visitLabel(endLabel)
      optTruthyDeclarationNode?.scope?.freeVariable(optTruthyDeclarationNode.name)
    } else {
      val falseStatementNode = node.falseStatementNode!!
      val falseLabel = Label()
      mv.jumpIfEq(falseLabel)
      node.trueStatementNode.accept(this)
      // this variable is only accessible in true statement, we don't need it after (especially for the else statement. it should
      //  be disposed before accepting it)
      optTruthyDeclarationNode?.scope?.freeVariable(optTruthyDeclarationNode.name)
      mv.jumpTo(endLabel)
      mv.visitLabel(falseLabel)
      falseStatementNode.accept(this)
      mv.visitLabel(endLabel)
    }
  }

  override fun visit(node: ElvisOperator) {
    val scope = node.scope
    val type = node.getType(typeResolver)

    val tempVar = scope.addLocalVariable(type, token = node.token)

    visitWithoutPushing(VariableAssignmentNode(node.token, scope, tempVar.name, node.leftOperand))
    val leftOperandRef = ReferenceExpression(node.token, scope, tempVar.name)
    visit(TernaryNode(node.token,
      BooleanExpressionNode.of(node.token, leftOperandRef),
      leftOperandRef, node.rightOperand
    ))
    scope.freeVariable(tempVar.name)
  }

  override fun visit(node: ConstructorCallNode) {
    if (node.type.primitive) {
      throw MarcelSemanticException(node.token, "Cannot instantiate a primitive type")
    }
    mv.visitConstructorCall(node)
  }

  override fun visit(node: NamedParametersConstructorCallNode) {
    mv.visitNamedConstructorCall(node)
  }

  override fun visit(node: SuperConstructorCallNode) {
    ownConstructorCallCheck(node)
    mv.visitSuperConstructorCall(node)
  }

  override fun visit(node: ThisConstructorCallNode) {
    ownConstructorCallCheck(node)
    mv.visitThisConstructorCall(node)
  }

  private fun ownConstructorCallCheck(node: AstNode) {
    if (!methodNode.isConstructor) {
      throw MarcelSemanticException(node.token, "Cannot call constructor in a non constructor method")
    }
    if ((methodNode.block.statements.firstOrNull() as? ExpressionStatementNode)?.expression !== node) {
      throw MarcelSemanticException(node.token, "Constructor call should be the first statement of a constructor")
    }
  }

  override fun visit(node: MulOperator) {
    arithmeticMarcelOperator(node, JavaPrimitiveType::mulCode)
  }

  override fun visit(node: DivOperator) {
    arithmeticMarcelOperator(node, JavaPrimitiveType::divCode)
  }

  override fun visit(node: MinusOperator) {
    arithmeticMarcelOperator(node, JavaPrimitiveType::subCode)
  }

  override fun visit(node: PlusOperator) {
    if (node.leftOperand.getType(typeResolver) == JavaType.String || node.rightOperand.getType(typeResolver) == JavaType.String) {
      StringNode.of(node.token, listOf(node.leftOperand, node.rightOperand)).accept(this)
    } else {
      arithmeticMarcelOperator(node, JavaPrimitiveType::addCode)
    }
  }

  override fun visit(node: PowOperator) {
    arithmeticMarcelOperator(node) {
      throw MarcelSemanticException(node.token, "Operator pow is not handled yet")
    }
  }

  override fun visit(node: LeftShiftOperator) {
    marcelOperator(node)
  }

  override fun visit(node: RightShiftOperator) {
    marcelOperator(node)
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
    val leftShiftMethod = typeResolver.findMethodOrThrow(type1, binaryOperatorNode.operatorMethodName!!, listOf(binaryOperatorNode.rightOperand.getType(typeResolver)), binaryOperatorNode)
    pushArgument(binaryOperatorNode.leftOperand)
    mv.invokeMethodWithArguments(binaryOperatorNode, methodNode.scope, leftShiftMethod, binaryOperatorNode.rightOperand)
    return leftShiftMethod.returnType
  }


  override fun visit(node: AsNode) {
    val expression = node.expressionNode
    if (expression is LiteralArrayNode) {
      val arrayType = if (node.type.isArray) node.type.asArrayType
      else if (node.type == JavaType.intList || node.type == JavaType.intSet) JavaType.intArray
      else if (node.type == JavaType.longList || node.type == JavaType.longSet) JavaType.longArray
      else if (node.type == JavaType.floatList || node.type == JavaType.floatSet) JavaType.floatArray
      else if (node.type == JavaType.doubleList || node.type == JavaType.doubleSet) JavaType.doubleArray
      else if (node.type == JavaType.charList || node.type == JavaType.characterSet) JavaType.charArray
      else if (node.type.raw() == List::class.javaType || node.type.raw() == Set::class.javaType) JavaType.objectArray
      else throw MarcelSemanticException(node.token, "Array cannot be converted into " + node.type)
      expression.type = arrayType
      // literal arrays can also be cast as collections (which will be handled in castIfNecessaryOrThrow
      if (expression.elements.isEmpty()) {
        visit(EmptyArrayNode(node.token, arrayType))
      } else {
        visit(expression)
      }
      mv.castIfNecessaryOrThrow(node.scope, node, node.type, arrayType)
    } else if (node.type == JavaType.boolean || node.type == JavaType.Boolean) {
      visit(BooleanExpressionNode.of(node.token, node.expressionNode))
      if (node.type == JavaType.Boolean) {
        mv.castIfNecessaryOrThrow(classNode.scope, node, JavaType.Boolean, JavaType.boolean)
      }
    } else {
      node.expressionNode.accept(this)
      mv.castIfNecessaryOrThrow(classNode.scope, node, node.type, node.expressionNode.getType(typeResolver))
    }
  }

  override fun visit(node: IsOperator) {
    if (node.leftOperand.getType(typeResolver).primitive || node.rightOperand.getType(typeResolver).primitive) {
      throw MarcelSemanticException(node.token, "Cannot apply '===' operator on primitive types")
    }
    pushBinaryOperatorOperands(node)
    val l1 = Label()
    mv.jump(Opcodes.IF_ACMPEQ, l1) // Jump if the two object references are equal

    mv.visitInsn(Opcodes.ICONST_0) // Load false on the stack
    val l2 = Label()
    mv.jumpTo(l2) // Jump to the end of the method
    mv.visitLabel(l1)
    mv.visitInsn(Opcodes.ICONST_1) // Load true on the stack
    mv.visitLabel(l2)
  }
  override fun visit(node: IsNotOperator) {
    if (node.leftOperand.getType(typeResolver).primitive || node.rightOperand.getType(typeResolver).primitive) {
      throw MarcelSemanticException(node.token, "Cannot apply '!==' operator on primitive types")
    }

    pushBinaryOperatorOperands(node)
    val l1 = Label()
    mv.jump(Opcodes.IF_ACMPNE, l1) // Jump if the two object references are equal

    mv.visitInsn(Opcodes.ICONST_0) // Load false on the stack
    val l2 = Label()
    mv.jumpTo(l2) // Jump to the end of the method
    mv.visitLabel(l1)
    mv.visitInsn(Opcodes.ICONST_1) // Load true on the stack
    mv.visitLabel(l2)
  }
  override fun visit(node: ComparisonOperatorNode) {
    val leftOperand = node.leftOperand
    val rightOperand = node.rightOperand
    val endLabel = Label()
    val trueLabel = Label()
    val operator = node.operator
    var objectcomparison = false
    if (!leftOperand.getType(typeResolver).primitive || !rightOperand.getType(typeResolver).primitive) {
      pushArgument(leftOperand)
      mv.castIfNecessaryOrThrow(classNode.scope, node, JavaType.Object, leftOperand.getType(typeResolver))
      pushArgument(rightOperand)
      mv.castIfNecessaryOrThrow(classNode.scope, node, JavaType.Object, rightOperand.getType(typeResolver))
      if ((leftOperand is NullValueNode || rightOperand is NullValueNode)) {
        objectcomparison = true
        if (operator != ComparisonOperator.EQUAL && operator != ComparisonOperator.NOT_EQUAL) {
          throw MarcelSemanticException(node.token, "Cannot compare null value with ${operator.symbolString} operator")
        }
      } else {
        when (operator) {
          ComparisonOperator.EQUAL, ComparisonOperator.NOT_EQUAL -> {
            mv.invokeMethod(node, classNode.scope, BytecodeHelper::class.java.getDeclaredMethod("objectsEqual", JavaType.Object.realClazz, JavaType.Object.realClazz))
            if (operator == ComparisonOperator.NOT_EQUAL) mv.not()
            return // the above method returns a boolean
          }
          else -> {
            val method = typeResolver.findMethodOrThrow(leftOperand.getType(typeResolver), "compareTo", listOf(rightOperand.getType(typeResolver)), node)
            if (method.returnType != JavaType.int) throw MarcelSemanticException(node.token, "compareTo method should return an int in order to be used in comparator")
            mv.invokeMethod(node, classNode.scope, method)
            mv.pushConstant(0) // pushing 0 because we're comparing two numbers below
          }
        }
      }
    } else if (leftOperand.getType(typeResolver) !in ComparisonOperator.INT_LIKE_COMPARABLE_TYPES || rightOperand.getType(typeResolver) !in ComparisonOperator.INT_LIKE_COMPARABLE_TYPES) {
      val otherType = if (leftOperand.getType(typeResolver) != JavaType.int) leftOperand.getType(typeResolver) else rightOperand.getType(typeResolver)
      pushArgument(leftOperand)
      mv.castIfNecessaryOrThrow(classNode.scope, node, otherType, leftOperand.getType(typeResolver))
      pushArgument(rightOperand)
      mv.castIfNecessaryOrThrow(classNode.scope, node, otherType, rightOperand.getType(typeResolver))
      when (otherType) {
        JavaType.double -> mv.visitInsn(Opcodes.DCMPL)
        JavaType.float -> mv.visitInsn(Opcodes.FCMPL)
        JavaType.long -> mv.visitInsn(Opcodes.LCMP)
        else -> throw UnsupportedOperationException("Doesn't handle comparison of primitive type $otherType")
      }
      mv.pushConstant(0) // pushing 0 because we're comparing two numbers below
    } else {
      pushBinaryOperatorOperands(node)
    }
    mv.jump(if (objectcomparison) node.operator.objectOpCode else node.operator.iOpCode, trueLabel)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.jumpTo(endLabel)
    mv.visitLabel(trueLabel)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitLabel(endLabel)
  }

  override fun visit(node: AndOperator) {
    val labelFalse = Label()
    val labelEnd = Label()
    pushArgument(node.leftOperand)
    mv.jumpIfEq(labelFalse)
    pushArgument(node.rightOperand)
    mv.jumpIfEq(labelFalse)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.jumpTo(labelEnd)
    mv.visitLabel(labelFalse)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitLabel(labelEnd)
  }

  override fun visit(node: OrOperator) {
    val labelTrue = Label()
    val labelFalse = Label()
    val labelEnd = Label()
    pushArgument(node.leftOperand)
    mv.jumpIfNe(labelTrue)
    pushArgument(node.rightOperand)
    mv.jumpIfEq(labelFalse)
    mv.visitLabel(labelTrue)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.jumpTo(labelEnd)
    mv.visitLabel(labelFalse)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitLabel(labelEnd)
  }

  override fun visit(node: FindOperator) {
    if (!CharSequence::class.java.javaType.isAssignableFrom(node.leftOperand.getType(typeResolver))) {
      throw MarcelSemanticException(node.token, "Left operand of find operator should be a string")
    }
    if (!Pattern::class.java.javaType.isAssignableFrom(node.rightOperand.getType(typeResolver))) {
      throw MarcelSemanticException(node.token, "Right operand of find operator should be a Pattern")
    }
    pushArgument(node.rightOperand)
    mv.invokeMethodWithArguments(node, classNode.scope, Pattern::class.java.getMethod("matcher", CharSequence::class.java), node.leftOperand)
  }

  override fun visit(node: InvokeAccessOperator) {
    val access = node.rightOperand

    if (node.nullSafe) {
      val scope = access.scope

      // need a local variable to avoid evaluating twice
      val tempVar = scope.addLocalVariable(node.leftOperand.getType(typeResolver), token = node.token)
      visitWithoutPushing(VariableAssignmentNode(node.token, scope, tempVar.name, node.leftOperand))
      val tempRef = ReferenceExpression(node.token, scope, tempVar.name)

      visit(TernaryNode(node.token,
        BooleanExpressionNode.of(node.token, ComparisonOperatorNode(node.token, ComparisonOperator.NOT_EQUAL, tempRef, NullValueNode(node.token))),
        // using a new function call because we need to use the tempRef instead of the actual leftOperand
        SimpleFunctionCallNode(node.token, access.scope, access.name, access.getArguments(typeResolver), access.getMethod(typeResolver)).apply {
          methodOwnerType = tempRef
        }
        , NullValueNode(node.token)
      ))
      scope.freeVariable(tempVar.name)
    } else {
      access.methodOwnerType = node.leftOperand
      access.accept(this)
    }
  }

  override fun visit(node: GetFieldAccessOperator) {
    val ownerType = node.leftOperand.getType(typeResolver)
    val field = typeResolver.findFieldOrThrow(ownerType, node.rightOperand.name, node)
    if (field.isStatic) {
      mv.getField(node, node.scope, field, node.directFieldAccess)
      return
    }
    if (node.nullSafe) {
      val scope = node.scope

      // need a local variable to avoid evaluating twice
      val tempVar = scope.addLocalVariable(ownerType, token = node.token)
      visitWithoutPushing(VariableAssignmentNode(node.token, scope, tempVar.name, node.leftOperand))
      val tempRef = ReferenceExpression(node.token, scope, tempVar.name)

      visit(TernaryNode(node.token,
        BooleanExpressionNode.of(node.token, ComparisonOperatorNode(node.token, ComparisonOperator.NOT_EQUAL, tempRef,
          NullValueNode(node.token))),
        // using a new GetFieldAccessOperator because we need to use the tempRef instead of the actual leftOperand
        GetFieldAccessOperator(node.token, tempRef, node.rightOperand, false, node.directFieldAccess)
        , NullValueNode(node.token)
      ))
      scope.freeVariable(tempVar.name)
    } else {
      pushArgument(node.leftOperand)
      mv.getField(node, node.scope, field, node.directFieldAccess)
    }
  }

  override fun visit(node: GetIndexFieldAccessOperator) {
    val field = typeResolver.findFieldOrThrow(node.leftOperand.getType(typeResolver), node.rightOperand.name, node)
    if (field.isStatic) {
      mv.getField(node, node.scope, field, node.directFieldAccess)
      mv.getAt(node, node.scope, node.rightOperand.variable.type, node.rightOperand.indexArguments)
      return
    }
    if (node.nullSafe) {
      val scope = node.scope

      // need a local variable to avoid evaluating twice
      val tempVar = scope.addLocalVariable(node.leftOperand.getType(typeResolver), token = node.token)
      visitWithoutPushing(VariableAssignmentNode(node.token, scope, tempVar.name, node.leftOperand))
      val tempRef = ReferenceExpression(node.token, scope, tempVar.name)

      visit(TernaryNode(node.token,
        BooleanExpressionNode.of(node.token, ComparisonOperatorNode(node.token, ComparisonOperator.NOT_EQUAL, tempRef,
          NullValueNode(node.token))),
        // using a new GetFieldAccessOperator because we need to use the tempRef instead of the actual leftOperand
        GetIndexFieldAccessOperator(node.token, tempRef, node.rightOperand, false, node.directFieldAccess)
        , NullValueNode(node.token)
      ))
      scope.freeVariable(tempVar.name)
    } else {
      pushArgument(node.leftOperand)
      mv.getField(node, node.scope, field, node.directFieldAccess)
      mv.getAt(node, node.scope, field.type, node.rightOperand.indexArguments)
    }
  }

  override fun visit(node: DirectFieldAccessNode) {
    val field = typeResolver.getClassField(node.scope.classType, node.name, node)
    pushArgument(ThisReference(node.token, node.scope))
    mv.getField(node, node.scope, field, true)
  }

  private fun pushBinaryOperatorOperands(binaryOperatorNode: BinaryOperatorNode) {
    pushArgument(binaryOperatorNode.leftOperand)
    pushArgument(binaryOperatorNode.rightOperand)
  }

  override fun visit(node: MethodDefaultParameterMethodCall) {
    val method = typeResolver.findMethod(node.ownerType, node.methodName, emptyList(), excludeInterfaces= true, node= node)
      ?: throw MarcelSemanticException(node.token, "Couldn't find default parameter value for method ${node.methodName} from classs ${node.ownerType}")

    visit(SimpleFunctionCallNode(node.token, classNode.scope, node.methodName, emptyList(), method))
  }

  override fun visit(node: FunctionCallNode) {
    val method = node.getMethod(typeResolver)
    val methodOwner = node.methodOwnerType
    if (!method.isInline) {
      if (!method.isStatic) {
        if (methodOwner is ExpressionNode) {
          pushArgument(methodOwner) // for instance method, we need to push owner
        } else {
          pushArgument(ReferenceExpression.thisRef(node.scope))
        }
      }
      mv.invokeMethodWithArguments(node, classNode.scope, method, node.getArguments(typeResolver))
    } else {
      // this probably doesn't work anymore, but hey, let's keep it for when I'll decide whether to implement this feature or not
      val inlineMethod = method as MethodNode
      val innerScope = InnerScope(
        node.scope as? MethodScope ?: throw MarcelSemanticException(node.token, "Can only call inline functions in a method"))
      val inlineBlock = inlineMethod.block.asSimpleBlock(inlineMethod.block.token, innerScope)
      inlineBlock.setTreeScope(innerScope)
      // initializing arguments
      if (node.getArguments(typeResolver).size != inlineMethod.parameters.size) {
        throw MarcelSemanticException(node.token, "Invalid number of arguments for method ${method.name}")
      }
      val variables = method.parameters.map { innerScope.addLocalVariable(it.type, it.name, token = node.token) }
      for (i in variables.indices) {
        visit(VariableAssignmentNode(node.token, innerScope, variables[i].name, node.getArguments(typeResolver)[i]))
      }
      visit(inlineBlock)
    }
    if (node.castType != null) {
      mv.castIfNecessaryOrThrow(node.scope, node, node.castType!!, method.actualReturnType)
    }
  }

  override fun visit(node: LambdaNode) {
    val constructorCall = lambdaHandler.defineLambda(node)
    visit(constructorCall)
  }

  override fun visit(assignmentNode: VariableAssignmentNode) {
    if (classNode.isScript && !assignmentNode.scope.hasVariable(assignmentNode.name)) {
      // we define the field dynamically for scripts
      typeResolver.defineField(classNode.type, BoundField(assignmentNode.expression.getType(typeResolver),
              assignmentNode.name, classNode.type))
    }

    val variable = assignmentNode.scope.findVariableOrThrow(assignmentNode.name, assignmentNode)
    // needed to smart cast literal arrays into lists
    val variableAssignmentNode = if (assignmentNode.expression is LiteralArrayNode) VariableAssignmentNode(assignmentNode.token, assignmentNode.scope, assignmentNode.name,
            AsNode(assignmentNode.token, assignmentNode.scope, variable.type, assignmentNode.expression))
    else assignmentNode
    if (variable is JavaField && !variable.isStatic) {
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
            assignmentNode.fieldNode.rightOperand.name, assignmentNode
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

  override fun visit(node: IndexedVariableAssignmentNode) {
    val indexedReference = node.indexedReference
    mv.storeInVariablePutAt(node,
      indexedReference.scope, indexedReference.variable,
      indexedReference.indexArguments, node.expression
    )
  }
  override fun visit(node: IndexedReferenceExpression) {
    if (node.isSafeIndex) {
      val funcCall = SimpleFunctionCallNode(node.token, node.scope, "getAtSafe",
        node.indexArguments.toMutableList(),
        ReferenceExpression(node.token, node.scope, node.name)
        )
       visit(funcCall)
      mv.castIfNecessaryOrThrow(classNode.scope, node, node.getType(typeResolver), funcCall.getType(typeResolver))
    } else {
      mv.pushVariableGetAt(node, node.scope, node.variable,
        node.indexArguments)
    }
  }

  override fun visit(node: VoidExpression) {
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

  override fun visit(node: WhileStatement) {
    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition
    node.condition.accept(pushingInstructionGenerator)
    val loopEnd = Label()
    mv.jumpIfEq(loopEnd)

    // loop body
    loopBody(node.body, loopStart, loopEnd)

    // Return to the beginning of the loop
    mv.jumpTo(loopStart)

    // loop end
    mv.visitLabel(loopEnd)
  }

  override fun visit(node: ForStatement) {
    // initialization
    node.initStatement.accept(this)

   // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition
    node.endCondition.accept(pushingInstructionGenerator)
    val loopEnd = Label()
    mv.jumpIfEq(loopEnd)

    // loop body
    val incrementLabel = Label()
    loopBody(node.body, incrementLabel, loopEnd)

    // iteration
    mv.visitLabel(incrementLabel)
    node.iteratorStatement.accept(this)
    mv.jumpTo(loopStart)

    // loop end
    mv.visitLabel(loopEnd)
  }

  override fun visit(node: TryCatchNode) {
    // TODO check
    //  - try block has at least one statement (if block)
    //  - at least catch or finally is here
    // TODO seems to work but compiled bytecode looks weird
    val finallyStatements = mutableListOf<StatementNode>()
    finallyStatements.addAll(node.resources.map {
      val scope = node.finallyBlock?.scope ?: InnerScope(node.scope)
      if (!it.type.implements(Closeable::class.javaType)) {
        throw MarcelSemanticException(node, "Try resources need to implement Closeable")
      }
      ExpressionStatementNode(node.token, SimpleFunctionCallNode(node.token, scope, "close", mutableListOf(),
        ReferenceExpression(node.token, scope, it.name),
        null,
        ))
    })
    if (finallyStatements.isEmpty()) {
      tryCatch(node)
    } else if (node.catchNodes.isEmpty()) {
      TODO("Try/Finally")
    } else {
      TODO("Try/Catch/Finally")
    }
  }

  private fun tryCatch(node: TryCatchNode) {
    val tryStart = Label()
    val tryEnd = Label()
    val endLabel = Label()
    val catchesWithLabel = node.catchNodes.map {
      // need one label for each catch block
      it to Label()
    }
    catchesWithLabel.forEach { c ->
      c.first.exceptionTypes.forEach { exceptionType ->
        if (!Throwable::class.javaType.isAssignableFrom(exceptionType)) {
          throw MarcelSemanticException(node.token, "Can only catch throwable")
        }
        mv.tryCatchBlock(tryStart, tryEnd, c.second, exceptionType)
      }
    }

    mv.visitLabel(tryStart)
    node.tryBlock.statementNode.accept(this)
    mv.visitLabel(tryEnd)
    mv.jumpTo(endLabel)

    catchesWithLabel.forEach { c ->
      val excVar = c.first.scope.addLocalVariable(JavaType.commonType(c.first.exceptionTypes), c.first.exceptionVarName, token = node.token)
      mv.catchBlock(c.second, excVar.index)
      c.first.statementNode.accept(this)
      mv.jumpTo(endLabel)
    }

    mv.visitLabel(endLabel)
  }

  override fun visit(node: ForInStatement) {
    val expression = node.inExpression
    val expressionType = expression.getType(typeResolver)
    if (expressionType.isArray) {
      arrayForEach(node, expressionType)
      return
    }

    // initialization
    val scope = node.scope
    scope.addLocalVariable(node.variableType, node.variableName, token = node.token)

    // creating iterator
    val iteratorExpression = if (Iterable::class.javaType.isAssignableFrom(expressionType)) SimpleFunctionCallNode(node.token, scope, "iterator", mutableListOf(), expression)
    else if (Iterator::class.javaType.isAssignableFrom(expressionType)) expression
    else if (CharSequence::class.javaType.isAssignableFrom(expressionType)) ConstructorCallNode(node.token, scope, CharSequenceIterator::class.java.javaType,
      mutableListOf(expression))
    else throw MarcelSemanticException(node.token, "Doesn't handle iterating on $expressionType")
    val iteratorExpressionType = iteratorExpression.getType(typeResolver)

    val iteratorVariable = scope.addLocalVariable(iteratorExpressionType, token = node.token)

    // get right method in function of types, to avoid auto-(un/debo)xing
    val methodName = if (IntIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextInt"
    else if (LongIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextLong"
    else if (FloatIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextFloat"
    else if (DoubleIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextDouble"
    else if (CharacterIterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "nextCharacter"
    else if (Iterator::class.javaType.isAssignableFrom(iteratorExpressionType)) "next"
    else throw UnsupportedOperationException("wtf")
    visit(VariableAssignmentNode(node.token, scope, iteratorVariable.name, iteratorExpression))

    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition
    val iteratorVarReference = ReferenceExpression(node.token, scope, iteratorVariable.name)
    pushArgument(iteratorVarReference)
    mv.invokeMethod(node, classNode.scope, IntIterator::class.java.getMethod("hasNext"))

    val loopEnd = Label()
    mv.jumpIfEq(loopEnd)

    // loop body
    visit(VariableAssignmentNode(node.token, scope, node.variableName, SimpleFunctionCallNode(node.token, scope, methodName, mutableListOf(), iteratorVarReference)))
    loopBody(node.body, loopStart, loopEnd)
    mv.jumpTo(loopStart)

    // loop end
    mv.visitLabel(loopEnd)

    // dispose
    scope.freeVariable(iteratorVariable.name)
  }
  private fun loopBody(body: BlockNode, continueLabel: Label, breakLabel: Label) {
    val scope = body.scope as? InnerScope ?: throw RuntimeException("Compiler design bug")
    scope.continueLabel = continueLabel
    scope.breakLabel = breakLabel
    body.accept(this)
  }

  private fun arrayForEach(node: ForInStatement, expressionType: JavaType) {
    // initialization
    val scope = node.scope
    // array expression
    val needsNewReference = node.inExpression !is ReferenceExpression
    val arrayVariable =
      if (needsNewReference) scope.addLocalVariable(type = expressionType, token = node.token)
      else (node.inExpression as ReferenceExpression).variable
    if (needsNewReference) visit(VariableAssignmentNode(node.token, scope, arrayVariable.name, node.inExpression))
    val arrayReference = ReferenceExpression(node.token, scope, arrayVariable.name)
    // index expression
    val indexVariable = scope.addLocalVariable(type = JavaType.int, token = node.token)
    val indexReference = ReferenceExpression(node.token, scope, indexVariable.name)
    visit(VariableAssignmentNode(node.token, scope, indexVariable.name, IntConstantNode(node.token, 0)))
    // variable expression
    scope.addLocalVariable(node.variableType, node.variableName, token = node.token)

    // loop start
    val loopStart = Label()
    mv.visitLabel(loopStart)

    // Verifying condition i < array.length
    pushArgument(ComparisonOperatorNode(node.token,
      ComparisonOperator.LT, indexReference,
      GetFieldAccessOperator(node.token, arrayReference,
        ReferenceExpression(node.token, scope, "length"), nullSafe = false, directFieldAccess = false)))

    val loopEnd = Label()
    mv.jumpIfEq(loopEnd)

    // loop body
    visit(VariableAssignmentNode(node.token, scope, node.variableName, IndexedReferenceExpression(node.token, scope, arrayVariable.name, listOf(indexReference), false)))
    loopBody(node.body, loopStart, loopEnd)
    visit(IncrNode(node.token, indexReference, 1, false))
    mv.jumpTo(loopStart)

    // loop end
    mv.visitLabel(loopEnd)

    // dispose
    scope.freeVariable(indexVariable.name)
    if (needsNewReference) scope.freeVariable(arrayVariable.name)
  }

  override fun visit(node: BreakLoopNode) {
    val label = node.scope.breakLabel ?: throw MarcelSemanticException(node.token, "Cannot use break statement outside of a loop")
    mv.jumpTo(label)
  }

  override fun visit(node: ContinueLoopNode) {
    val label = node.scope.continueLabel ?: throw MarcelSemanticException(node.token, "Cannot use continue statement outside of a loop")
    mv.jumpTo(label)
  }

  override fun visit(node: LambdaNode) {
    super.visit(node)
    mv.popStack()
  }
  override fun visit(node: GetFieldAccessOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: GetIndexFieldAccessOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: DirectFieldAccessNode) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: IntConstantNode) {
    // don't need to write constants
  }

  override fun visit(node: ShortConstantNode) {
    // don't need to write constants
  }

  override fun visit(node: ByteConstantNode) {
    // don't need to write constants
  }
  override fun visit(node: LongConstantNode) {
    // don't need to write constants
  }

  override fun visit(node: FloatConstantNode) {
    // don't need to write constants
  }

  override fun visit(node: DoubleConstantNode) {
    // don't need to write constants
  }

  override fun visit(node: CharConstantNode) {
    // don't need to write constants
  }
  override fun visit(node: BooleanConstantNode) {
    // don't need to write constants
  }
  override fun visit(node: StringConstantNode) {
    // don't need to write constants
  }

  override fun visit(node: ThisReference) {
    // don't need to write this if it isn't used
  }


  override fun visit(node: LiteralPatternNode) {
    // don't need to write this if it isn't used
  }

  override fun visit(node: SuperReference) {
    // don't need to write super if it isn't used
  }
  override fun visit(node: ConstructorCallNode) {
    super.visit(node)
    mv.popStack() // don't really know if it's necessary
  }

  override fun visit(node: NamedParametersConstructorCallNode) {
    super.visit(node)
    mv.popStack() // don't really know if it's necessary
  }

  override fun visit(node: FunctionCallNode) {
    super.visit(node)
    if (node.getType(typeResolver) != JavaType.void) {
      mv.popStack() // don't really know if it's necessary
    }
  }
  override fun visit(node: ToStringNode) {
    node.expressionNode.accept(this)
  }

  override fun visit(node: StringNode) {
    for (part in node.parts) {
      part.accept(this)
    }
  }

  override fun visit(node: RangeNode) {
    pushingInstructionGenerator.visit(node)
    mv.popStack()
  }

  override fun visit(node: LiteralArrayNode) {
    node.elements.forEach { it.accept(this) }
  }

  override fun visit(node: LiteralMapNode) {
    node.entries.forEach {
      it.first.accept(this)
      it.second.accept(this)
    }
  }
  override fun visit(node: BooleanExpressionNode) {
    node.innerExpression.accept(this)
  }

  override fun visit(node: IncrNode) {
    if (node.variableReference.getType(typeResolver) == JavaType.int && node.variableReference.variable is LocalVariable) {
      mv.incrLocalVariable(node.variableReference.variable as LocalVariable, node.amount)
    } else {
      val ref = node.variableReference
      visit(VariableAssignmentNode(node.token, ref.scope, ref.name, PlusOperator(node.token, ref, IntConstantNode(node.token, node.amount))))
    }
  }

  override fun visit(node: NullValueNode) {
    // no need to push anything
  }

  override fun visit(node: ClassExpressionNode) {
    // no need to push anything
  }

  override fun visit(node: ReferenceExpression) {
    // don't need to push value to the stack by default
  }

  override fun visit(node: IndexedReferenceExpression) {
    super.visit(node)
    mv.popStack()
  }

  override fun pushArgument(expr: ExpressionNode) {
    pushingInstructionGenerator.pushArgument(expr)
  }


  override fun visit(node: FunctionBlockNode) {
    for (i in 0..(node.statements.size - 2)) {
      node.statements[i].accept(this)
    }
    val lastStatement = node.statements.lastOrNull()
    if (lastStatement is ReturnNode) {
      // if this is return node, there is no ambiguity here. Just process it
      lastStatement.accept(this)
      return
    }
    if (node.scope.returnType == JavaType.void) {
      lastStatement?.accept(this)
      mv.returnVoid()
    } else {
      if (lastStatement is ExpressionStatementNode && lastStatement.expression.getType(typeResolver) != JavaType.void) {
        visit(ReturnNode(node.token, node.scope, lastStatement.expression))
      } else if (!node.scope.returnType.primitive) {
        lastStatement?.accept(this)
        // just return null
        mv.pushNull()
        mv.returnCode(node.scope.returnType.returnCode)
      } else if (lastStatement == null || !lastStatement.allBranchesReturn()) {
        throw MarcelSemanticException(node.token, "Function returning primitive types must explicitly return an expression as the last statement." +
            "You must explicitly return an expression, a switch or a when.")
      } else {
        lastStatement.accept(this)
        mv.castIfNecessaryOrThrow(classNode.scope, node, node.scope.returnType, lastStatement.getType(typeResolver))
        mv.returnCode(node.scope.returnType.returnCode)
      }
    }
  }

  override fun visit(node: MulOperator) {
    super.visit(node)
    mv.popStack()
  }
  override fun visit(node: DivOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: MinusOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: PlusOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: RightShiftOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: LeftShiftOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: PowOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: ComparisonOperatorNode) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: IsOperator) {
    super.visit(node)
    mv.popStack()
  }
  override fun visit(node: IsNotOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: AndOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: FindOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: OrOperator) {
    super.visit(node)
    mv.popStack()
  }

  override fun visit(node: NotNode) {
    node.operand.accept(this)
  }

  override fun visit(node: ExpressionStatementNode) {
    node.expression.accept(this)
  }

  override fun visit(node: VariableDeclarationNode) {
    node.scope.addLocalVariable(node.type, node.name,
      node.isFinal, node.token)
    visit(node as VariableAssignmentNode)
  }

  override fun visit(node: MultiVariableDeclarationNode) {
    val scope = node.scope
    val expressionType = node.expression.getType(typeResolver)
    if (!List::class.javaType.isAssignableFrom(expressionType) && !expressionType.isArray) {
      throw MarcelSemanticException(node.token, "Multi variable declarations must use an array or a list as the expression")
    }
    val tempVar = scope.addLocalVariable(expressionType, token = node.token)
    // assign expression to variable
    visit(VariableAssignmentNode(node.token, scope, tempVar.name, node.expression))
    // then process each variable declarations
    for (i in node.declarations.indices) {
      val declaration = node.declarations[i] ?: continue
      visit(VariableDeclarationNode(node.token, scope, declaration.first, declaration.second, false,
        IndexedReferenceExpression(node.token, scope, tempVar.name, listOf(IntConstantNode(node.token, i)), false)))
    }
    scope.freeVariable(tempVar.name)
  }

  override fun visit(node: TruthyVariableDeclarationNode) {
    pushArgument(node)
    mv.popStack()
  }

  override fun visit(node: BlockNode) {
    for (statement in node.statements) {
      statement.accept(this)
    }
  }

  override fun visit(node: ReturnNode) {
    if (node.scope.returnType == JavaType.void && node.expression !is VoidExpression) {
      throw MarcelSemanticException(node.token, "Cannot return an expression in a void function")
    }
    pushArgument(node.expression)
    mv.castIfNecessaryOrThrow(classNode.scope, node, node.scope.returnType, node.expression.getType(typeResolver))
    mv.returnCode(node.scope.returnType.returnCode)
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


  override fun visit(node: ForStatement) {
    instructionGenerator.visit(node)
  }

  override fun visit(node: TryCatchNode) {
    instructionGenerator.visit(node)
  }

  override fun visit(node: ForInStatement) {
    instructionGenerator.visit(node)
  }

  override fun visit(node: LiteralArrayNode) {
    mv.newArray(classNode.scope, node, node.getType(typeResolver).asArrayType, node.elements)
  }

  override fun visit(node: LiteralMapNode) {
    val mapType = HashMap::class.javaType
    val method = typeResolver.findMethodOrThrow(mapType, JavaMethod.CONSTRUCTOR_NAME, emptyList(), node)
    mv.visitConstructorCall(node, mapType, method, classNode.scope, emptyList())

    for (entry in node.entries) {
      mv.dup()
      pushArgument(entry.first)
      mv.castIfNecessaryOrThrow(classNode.scope, node, JavaType.Object, entry.first.getType(typeResolver))
      pushArgument(entry.second)
      mv.castIfNecessaryOrThrow(classNode.scope, node, JavaType.Object, entry.second.getType(typeResolver))
      mv.invokeMethod(node, classNode.scope, typeResolver.findMethodOrThrow(mapType,
        "put",
        listOf(JavaType.Object, JavaType.Object), node
      ))
      mv.popStack()
    }
  }

  override fun visit(node: RangeNode) {
    val methodName = if (node.fromExclusive && node.toExclusive) "ofExclusive"
    else if (node.fromExclusive) "ofFromExclusive"
    else if (node.toExclusive) "ofToExclusive"
    else "of"
    val fromType = node.from.getType(typeResolver)
    val toType = node.to.getType(typeResolver)
    val method =
      if (fromType == JavaType.long || fromType == JavaType.Long
        || toType == JavaType.long || toType == JavaType.Long) ReflectJavaMethod(LongRanges::class.java.getMethod(methodName, Long::class.java, Long::class.java))
      else ReflectJavaMethod(IntRanges::class.java.getMethod(methodName, Int::class.java, Int::class.java))
    mv.invokeMethodWithArguments(node, classNode.scope, method, node.from, node.to)
  }

  override fun visit(node: NotNode) {
    when (node.operand.getType(typeResolver)) {
      JavaType.Boolean -> mv.invokeMethodWithArguments(node, classNode.scope, typeResolver.findMethodOrThrow(JavaType.Boolean, "booleanValue", emptyList()), node.operand, node)
      JavaType.boolean -> node.operand.accept(this)
      else -> visit(BooleanExpressionNode.of(node.token, node.operand))
    }
    mv.not()
  }
  override fun visit(node: WhileStatement) {
    instructionGenerator.visit(node)
  }
  override fun visit(node: StringConstantNode) {
    mv.pushConstant(node.value)
  }

  override fun visit(node: BreakLoopNode) {
    instructionGenerator.visit(node)
  }

  override fun visit(node: ContinueLoopNode) {
    instructionGenerator.visit(node)
  }
  override fun visit(node: BooleanExpressionNode) {
    val innerType = node.innerExpression.getType(typeResolver)
    if (node.innerExpression is NullValueNode) {
      visit(BooleanConstantNode(node.token, false))
    } else if (innerType == JavaType.boolean
      || innerType == JavaType.Boolean) {
      node.innerExpression.accept(this)
      mv.castIfNecessaryOrThrow(classNode.scope, node, JavaType.boolean, innerType)
    } else if (innerType.primitive) {
      // according to marcel truth, all primitive are truthy
      node.innerExpression.accept(instructionGenerator)
      visit(BooleanConstantNode(node.token, true))
    } else if (Matcher::class.javaType.isAssignableFrom(innerType)) {
      pushArgument(node.innerExpression)
      mv.invokeMethod(node, classNode.scope, Matcher::class.java.getMethod("find"))
    } else {
      val classTruthyMethod = typeResolver.findMethod(innerType, "isTruthy", emptyList(), false, node)
      if (classTruthyMethod != null) {
        pushArgument(node.innerExpression)
        mv.invokeMethod(node.innerExpression, classNode.scope, classTruthyMethod)
      } else {
        // this is a static method. No need to push owner
        val marcelTruthyMethod = typeResolver.findMethodOrThrow(MarcelTruth::class.javaType, "truthy", listOf(innerType), node)
        mv.invokeMethodWithArguments(node, classNode.scope, marcelTruthyMethod, node.innerExpression)
      }
    }
  }

  override fun visit(node: ToStringNode) {
    val expr = node.expressionNode
    if (expr.getType(typeResolver) == JavaType.String) {
      expr.accept(this)
    } else {
      val argumentType = expr.getType(typeResolver)
      if (argumentType.primitive) {
        mv.invokeMethodWithArguments(node, classNode.scope, String::class.java.getDeclaredMethod("valueOf", argumentType.realClazz), expr)
      } else {
        mv.invokeMethodWithArguments(node, classNode.scope, String::class.java.getDeclaredMethod("valueOf", JavaType.Object.realClazz), expr)
      }
    }
  }
  override fun visit(node: StringNode) {
    if (node.parts.isEmpty()) {
      // empty string
      StringConstantNode(node.token, "").accept(this)
      return
    } else if (node.parts.size == 1) {
      ToStringNode.of(node.token, node.parts.first()).accept(this)
      return
    }
    // new StringBuilder() can just provide an empty new scope as we'll just use it to extract the method from StringBuilder which already exists in the JDK
    val type = StringBuilder::class.javaType
    visit(ConstructorCallNode(node.token, Scope(typeResolver, type, false), type, mutableListOf()))
    for (part in node.parts) {
      // chained calls
      val argumentType = part.getType(typeResolver)
      val method = ReflectJavaMethod(StringBuilder::class.java.getDeclaredMethod("append",
        if (argumentType.primitive) argumentType.realClazz else JavaType.Object.realClazz))
      mv.invokeMethodWithArguments(node, classNode.scope, method, part)
    }
    mv.invokeMethod(node, classNode.scope, StringBuilder::class.java.getDeclaredMethod("toString"))
  }

  override fun visit(node: IntConstantNode) {
    mv.pushConstant(node.value)
  }

  override fun visit(node: LongConstantNode) {
    mv.pushConstant(node.value)
  }

  override fun visit(node: ShortConstantNode) {
    mv.pushConstant(node.value)
  }

  override fun visit(node: ByteConstantNode) {
    mv.pushConstant(node.value)
  }
  override fun visit(node: FloatConstantNode) {
    mv.pushConstant(node.value)
  }

  override fun visit(node: DoubleConstantNode) {
    mv.pushConstant(node.value)
  }

  override fun visit(node: CharConstantNode) {
    val value = node.value
    if (value.length != 1) throw MarcelSemanticException(node.token, "Characters should be strings of exactly one char")
    mv.pushConstant(value[0])
  }
  override fun visit(node: NullValueNode) {
    mv.pushNull()
  }

  override fun visit(node: SuperReference) {
    if (methodNode.isStatic) throw MarcelSemanticException(node.token, "Cannot reference 'super' in a static context")
    mv.pushThis() // super is actually this. The difference is in the class internalName supplied when performing ASM instructions
  }

  override fun visit(node: ThisReference) {
    when {
      !methodNode.isStatic -> mv.pushThis()
      // for extension class
      classNode.isExtensionClass && node.scope.hasVariable("self") -> visit(ReferenceExpression(node.token, node.scope, "self"))
      else -> throw MarcelSemanticException(node.token, "Cannot reference 'this' in a static context")
    }
  }

  override fun visit(node: LiteralPatternNode) {
    mv.pushConstant(node.value)
    if (node.flags.isNotEmpty()) {
      val flag = node.flags.reduce { acc, i -> acc or i }
      mv.pushConstant(flag)
      mv.invokeMethod(node, classNode.scope, Pattern::class.java.getMethod("compile", String::class.java, Int::class.java))
    } else {
      mv.invokeMethod(node, classNode.scope, Pattern::class.java.getMethod("compile", String::class.java))
    }
  }
  override fun visit(node: IncrNode) {
    if (node.returnValueBefore) {
      mv.pushVariable(node, node.variableReference.scope, node.variableReference.variable)
      instructionGenerator.visit(node)
    } else {
      instructionGenerator.visit(node)
      mv.pushVariable(node, node.variableReference.scope, node.variableReference.variable)
    }
  }
  override fun visit(node: BooleanConstantNode) {
    mv.pushConstant(node.value)
  }
  override fun visit(node: ReferenceExpression) {
    mv.pushVariable(node, node.scope, node.variable)
  }

  override fun visit(node: VariableAssignmentNode) {
    super.visit(node)
    mv.pushVariable(node, node.scope, node.scope.findVariableOrThrow(node.name, node))
  }

  override fun visit(node: FieldAssignmentNode) {
    super.visit(node)
    val field = typeResolver.findFieldOrThrow(node.fieldNode.leftOperand.getType(typeResolver), node.fieldNode.rightOperand.name, node)
    if (!field.isStatic) {
      pushArgument(node.fieldNode.leftOperand)
    }
    mv.pushVariable(node, node.scope, field)
  }

  override fun visit(node: IndexedVariableAssignmentNode) {
    super.visit(node)
    pushArgument(node.indexedReference)
  }
  override fun pushArgument(expr: ExpressionNode) {
    expr.accept(this)
  }

  override fun visit(node: ReturnNode) {
    instructionGenerator.visit(node)
  }

  override fun visit(node: ExpressionStatementNode) {
    node.expression.accept(this)
  }

  override fun visit(node: VariableDeclarationNode) {
    instructionGenerator.visit(node)
  }

  override fun visit(node: MultiVariableDeclarationNode) {
    instructionGenerator.visit(node)
  }
  override fun visit(node: TruthyVariableDeclarationNode) {
    var actualTruthyVariableDeclarationNode = node
    val variableType = actualTruthyVariableDeclarationNode.variableType
    val expressionType = actualTruthyVariableDeclarationNode.expression.getType(typeResolver)
    if (variableType.raw() != Optional::class.javaType && (
          listOf(Optional::class.javaType, OptionalInt::class.javaType, OptionalLong::class.javaType, OptionalDouble::class.javaType)
            .any {expressionType.raw().isAssignableFrom(it) }
          )) {
      actualTruthyVariableDeclarationNode = TruthyVariableDeclarationNode(node.token,
        actualTruthyVariableDeclarationNode.scope, actualTruthyVariableDeclarationNode.variableType, actualTruthyVariableDeclarationNode.name,
        InvokeAccessOperator(node.token, actualTruthyVariableDeclarationNode.expression,
          SimpleFunctionCallNode(node.token, actualTruthyVariableDeclarationNode.scope, "orElse", mutableListOf(NullValueNode(node.token, actualTruthyVariableDeclarationNode.variableType)))
        , false)
      )
    }
    // declaring truthy variable (will and should only be used in trueStatement)
    visit(VariableDeclarationNode(actualTruthyVariableDeclarationNode.token, actualTruthyVariableDeclarationNode.scope, variableType, actualTruthyVariableDeclarationNode.name,
      actualTruthyVariableDeclarationNode.isFinal, actualTruthyVariableDeclarationNode.expression))

    if (actualTruthyVariableDeclarationNode.variableType.primitive) {
      visit(BooleanConstantNode(node.token, true))
    } else {
      pushArgument(BooleanExpressionNode.of(node.token,
        ReferenceExpression(node.token, actualTruthyVariableDeclarationNode.scope, actualTruthyVariableDeclarationNode.name)
      ))
    }
  }
  override fun visit(node: BlockNode) {
    instructionGenerator.visit(node)
  }

  override fun visit(node: FunctionBlockNode) {
    instructionGenerator.visit(node)
  }

  override fun visit(node: ClassExpressionNode) {
    mv.pushClass(node, methodNode.scope, node.clazz)
  }

  override fun visitWithoutPushing(astNode: AstInstructionNode) {
    astNode.accept(instructionGenerator)
  }
}