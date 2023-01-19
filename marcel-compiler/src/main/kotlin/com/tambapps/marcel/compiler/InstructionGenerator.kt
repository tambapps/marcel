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
import com.tambapps.marcel.parser.ast.expression.IntConstantNode
import com.tambapps.marcel.parser.ast.expression.MinusOperator
import com.tambapps.marcel.parser.ast.expression.MulOperator
import com.tambapps.marcel.parser.ast.expression.NullValueNode
import com.tambapps.marcel.parser.ast.expression.PlusOperator
import com.tambapps.marcel.parser.ast.expression.PowOperator
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
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.ForStatement
import com.tambapps.marcel.parser.ast.statement.IfStatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.ast.statement.WhileStatement
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.InnerScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaPrimitiveType
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.PrintStream

// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5.if_icmp_cond
// https://asm.ow2.io/asm4-guide.pdf
// https://en.wikipedia.org/wiki/List_of_Java_bytecode_instructions
private interface IInstructionGenerator: AstNodeVisitor {

  val mv: MethodVisitor


  //TODO don't forget to push or not these TODOs() once done, based on the IUnpushedExpressionGenerator implementation
  override fun visit(unaryMinus: UnaryMinus) {
    TODO("Not yet implemented")
  }

  override fun visit(unaryPlus: UnaryPlus) {
    TODO("Not yet implemented")
  }

  override fun visit(operator: TernaryNode) {
    TODO("Not yet implemented")
  }

  override fun visit(fCall: ConstructorCallNode) {
    if (fCall.type.primitive) {
      throw SemanticException("Cannot instantiate a primitive type")
    }
    val classInternalName = fCall.type.internalName
    mv.visitTypeInsn(Opcodes.NEW, classInternalName)
    mv.visitInsn(Opcodes.DUP)
    pushFunctionCallArguments(fCall)
    val constructorMethod = fCall.scope.getMethodForType(fCall.type, JavaMethod.CONSTRUCTOR_NAME, fCall.arguments)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, classInternalName, fCall.name,
        constructorMethod.descriptor, false)
  }

  override fun visit(fCall: SuperConstructorCallNode) {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    pushFunctionCallArguments(fCall)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, fCall.scope.superClassInternalName, fCall.name,
      AsmUtils.getDescriptor(fCall.arguments, JavaType.void), false)
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
      mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")

      if (fCall.parameterTypes.size != 1) {
        throw SemanticException("Invalid call of println")
      }
      val argumentClass = fCall.arguments.first().type.realClassOrObject
      val method = PrintStream::class.java.getDeclaredMethod("println", if (argumentClass.isPrimitive) argumentClass else Object::class.java)
      for (argumentNode in fCall.arguments) {
        // write argument on the stack
        pushArgument(argumentNode)
      }
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", AsmUtils.getDescriptor(method), false)
    } else {
      val method = fCall.method
      val methodOwner = fCall.methodOwnerType
      if (methodOwner is ExpressionNode) {
       pushArgument(methodOwner) // for instance method, we need to push owner
      } else if (!method.isStatic) {
        pushArgument(VariableReferenceExpression(fCall.scope, "this"))
      }
      pushFunctionCallArguments(fCall)
      mv.visitMethodInsn(method.invokeCode, method.ownerClass.internalName, fCall.name, method.descriptor, false)
    }
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    pushArgument(variableAssignmentNode.expression)
    val (variable, index) = variableAssignmentNode.scope.getLocalVariableWithIndex(variableAssignmentNode.name)
    if (!variable.type.isAssignableFrom(variableAssignmentNode.expression.type)) {
      throw SemanticException("Incompatible types")
    }
    mv.visitVarInsn(variable.type.storeCode, index)
  }

  override fun visit(voidExpression: VoidExpression) {
    // do nothing, it's void
  }
  fun pushArgument(expr: ExpressionNode)
}

/**
 * Generates expression bytecode but don't push them to the stack. (Useful for statement expressions)
 */
class InstructionGenerator(override val mv: MethodVisitor): IInstructionGenerator {

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
    mv.visitJumpInsn(Opcodes.IFEQ, loopEnd)

    // loop body
    whileStatement.body.accept(this)

    // Return to the beginning of the loop
    mv.visitJumpInsn(Opcodes.GOTO, loopStart)

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
    mv.visitJumpInsn(Opcodes.IFEQ, loopEnd)

    // loop body
    forStatement.statement.accept(this)

    // iteration
    forStatement.iteratorStatement.accept(this)
    mv.visitJumpInsn(Opcodes.GOTO, loopStart)

    // loop end
    mv.visitLabel(loopEnd)
  }
  override fun visit(ifStatementNode: IfStatementNode) {
    ifStatementNode.condition.accept(pushingInstructionGenerator)
    val endLabel = Label()
    if (ifStatementNode.falseStatementNode == null) {
      mv.visitJumpInsn(Opcodes.IFEQ, endLabel)
      ifStatementNode.trueStatementNode.accept(this)
      mv.visitLabel(endLabel)
    } else {
      val falseStatementNode = ifStatementNode.falseStatementNode!!
      val falseLabel = Label()
      mv.visitJumpInsn(Opcodes.IFEQ, falseLabel)
      ifStatementNode.trueStatementNode.accept(this)
      mv.visitJumpInsn(Opcodes.GOTO, endLabel)
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
    mv.visitInsn(Opcodes.POP) // don't really know if it's necessary
  }
  override fun visit(fCall: FunctionCallNode) {
    super.visit(fCall)
    if (fCall.type != JavaType.void) {
      mv.visitInsn(Opcodes.POP) // don't really know if it's necessary
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

  override fun visit(booleanExpression: BooleanExpressionNode) {
    booleanExpression.innerExpression.accept(this)
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
      mv.visitInsn(Opcodes.RETURN)
    } else {
      if (lastStatement.type != JavaType.void) {
        pushArgument(lastStatement.expression)
      } else {
        // method expects an object but nothing was returned? let's return null
        mv.visitInsn(Opcodes.ACONST_NULL)
      }
      mv.visitInsn(blockNode.scope.returnType.returnCode)
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
  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(this)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    variableDeclarationNode.scope.addLocalVariable(variableDeclarationNode.type, variableDeclarationNode.name)
    visit(variableDeclarationNode as VariableAssignmentNode)
  }

  fun drop2() {
    // TODO verify it does what I think
    mv.visitInsn(Opcodes.POP2)
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

private class PushingInstructionGenerator(override val mv: MethodVisitor): IInstructionGenerator {
  lateinit var instructionGenerator: InstructionGenerator


  override fun visit(forStatement: ForStatement) {
    instructionGenerator.visit(forStatement)
  }

  override fun visit(whileStatement: WhileStatement) {
    instructionGenerator.visit(whileStatement)
  }
  override fun visit(ifStatementNode: IfStatementNode) {
    instructionGenerator.visit(ifStatementNode)
  }
  override fun visit(stringConstantNode: StringConstantNode) {
    mv.visitLdcInsn(stringConstantNode.value)
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
    if (expr.type == JavaType.STRING) {
      expr.accept(this)
    } else {
      val argumentClass = expr.type.realClassOrObject
      val method = String::class.java.getDeclaredMethod("valueOf", if (argumentClass.isPrimitive) argumentClass else Object::class.java)
      pushArgument(expr)
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", AsmUtils.getDescriptor(method), false)
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
      val method = StringBuilder::class.java.getDeclaredMethod("append", if (argumentClass.isPrimitive) argumentClass else Object::class.java)
      pushArgument(part)
      mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", AsmUtils.getDescriptor(method), false)
    }
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", AsmUtils.getDescriptor(emptyList(), JavaType.STRING), false)
  }

  override fun visit(integer: IntConstantNode) {
    mv.visitLdcInsn(integer.value) // write primitive value, from an Object class e.g. Integer -> int
  }

  override fun visit(nullValueNode: NullValueNode) {
    mv.visitInsn(Opcodes.ACONST_NULL)
  }

  override fun visit(booleanConstantNode: BooleanConstantNode) {
    mv.visitInsn(if (booleanConstantNode.value) Opcodes.ICONST_1 else Opcodes.ICONST_0)
  }
  override fun visit(variableReferenceExpression: VariableReferenceExpression) {
    pushVariable(variableReferenceExpression.scope, variableReferenceExpression.name)
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    super.visit(variableAssignmentNode)
    pushVariable(variableAssignmentNode.scope, variableAssignmentNode.name)
  }

  private fun pushVariable(scope: Scope, variableName: String) {
    val (variable, index) = scope.getLocalVariableWithIndex(variableName)
    mv.visitVarInsn(variable.type.loadCode, index)
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
    mv.visitJumpInsn(comparisonOperator.operator.iOpCode, trueLabel)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitJumpInsn(Opcodes.GOTO, endLabel)
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