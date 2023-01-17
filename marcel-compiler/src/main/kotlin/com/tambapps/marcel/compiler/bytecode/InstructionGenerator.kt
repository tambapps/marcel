package com.tambapps.marcel.compiler.bytecode

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstNodeVisitor
import com.tambapps.marcel.parser.ast.expression.*
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.VariableDeclarationNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.PrintStream
import java.lang.StringBuilder

private interface IInstructionGenerator: AstNodeVisitor {

  val mv: MethodVisitor
  val scope: Scope


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
    val constructorMethod = scope.getMethod("<init>", fCall.arguments)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, classInternalName, fCall.name,
        constructorMethod.descriptor, false)

  }
  override fun visit(fCall: SuperConstructorCallNode) {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    pushFunctionCallArguments(fCall)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, scope.superClassInternalName, fCall.name,
      AsmUtils.getDescriptor(fCall.arguments, JavaType.void), false)
  }

  private fun pushFunctionCallArguments(fCall: FunctionCallNode) {
    val method = scope.getMethod(fCall.name, fCall.arguments)
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

  private fun evaluateOperands(binaryOperatorNode: BinaryOperatorNode) {
    pushArgument(binaryOperatorNode.leftOperand)
    pushArgument(binaryOperatorNode.rightOperand)
  }

  override fun visit(fCall: FunctionCallNode) {
    if (fCall.name == "println") {
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
      val method = scope.getMethod(fCall.name, fCall.arguments)
      pushFunctionCallArguments(fCall)

      // TODO might need to push on stack variable/expression, if owner is not static, and need not to push for unpushedblabla
      val owner = method.owner
      mv.visitMethodInsn(owner.invokeCode, owner.classInternalName, fCall.name, method.descriptor, false)
    }
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    pushArgument(variableAssignmentNode.expression)
    val (variable, index) = scope.getLocalVariableWithIndex(variableAssignmentNode.name)
    if (variable.type != variableAssignmentNode.expression.type) {
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
class InstructionGenerator(override val mv: MethodVisitor, override val scope: Scope): IInstructionGenerator {

  private val pushingInstructionGenerator = PushingInstructionGenerator(mv, scope)

  init {
    pushingInstructionGenerator.instructionGenerator = this
  }

  override fun visit(integer: IntConstantNode) {
    // don't need to write constants
  }

  override fun visit(stringConstantNode: StringConstantNode) {
    // don't need to write constants
  }

  override fun visit(toStringNode: ToStringNode) {
    toStringNode.expressionNode.accept(this)
  }
  override fun visit(stringNode: StringNode) {
    for (part in stringNode.parts) {
      part.accept(this)
    }
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
    if (blockNode.methodReturnType == JavaType.void) {
      lastStatement.accept(this)
      mv.visitInsn(Opcodes.RETURN)
    } else {
      pushArgument(lastStatement.expression)
      mv.visitInsn(blockNode.type.returnCode)
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

  override fun visit(expressionStatementNode: ExpressionStatementNode) {
    expressionStatementNode.expression.accept(this)
  }

  override fun visit(variableDeclarationNode: VariableDeclarationNode) {
    visit(variableDeclarationNode as VariableAssignmentNode)
  }

  fun drop2() {
    TODO("Drop 2 args from the stack")
  }

  override fun visit(blockNode: BlockNode) {
    for (statement in blockNode.statements) {
      statement.accept(this)
    }
  }

  override fun visit(returnNode: ReturnNode) {
    // should never be called. Blocks will always take care of return statements and they will use pushArgument
    throw IllegalStateException("Compiler design error, sorry for that")
  }
}

private class PushingInstructionGenerator(override val mv: MethodVisitor, override val scope: Scope): IInstructionGenerator {
  lateinit var instructionGenerator: InstructionGenerator


  override fun visit(stringConstantNode: StringConstantNode) {
    mv.visitLdcInsn(stringConstantNode.value)
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
    // new StringBuilder()
    instructionGenerator.visit(ConstructorCallNode(JavaType(StringBuilder::class.java), mutableListOf()))
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

  override fun visit(variableReferenceExpression: VariableReferenceExpression) {
    pushVariable(variableReferenceExpression.name)
  }

  override fun visit(variableAssignmentNode: VariableAssignmentNode) {
    super.visit(variableAssignmentNode)
    pushVariable(variableAssignmentNode.name)
  }

  private fun pushVariable(variableName: String) {
    val (variable, index) = scope.getLocalVariableWithIndex(variableName)
    mv.visitVarInsn(variable.type.loadCode, index)
  }

  override fun pushArgument(expr: ExpressionNode) {
    expr.accept(this)
  }

  override fun visit(operator: MulOperator) {
    super.visit(operator)
    mv.visitInsn(operator.type.mulCode)
  }

  override fun visit(operator: DivOperator) {
    super.visit(operator)
    mv.visitInsn(operator.type.divCode)
  }

  override fun visit(operator: MinusOperator) {
    super.visit(operator)
    mv.visitInsn(operator.type.subCode)
  }


  override fun visit(operator: PlusOperator) {
    super.visit(operator)
    mv.visitInsn(operator.type.addCode)
  }

  override fun visit(operator: PowOperator) {
    super.visit(operator)
    TODO("Implement pow, or call function?")
  }

  override fun visit(returnNode: ReturnNode) {
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