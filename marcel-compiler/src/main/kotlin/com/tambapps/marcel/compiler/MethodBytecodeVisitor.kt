package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.IncrNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.VariableAssignmentNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.PrintStream
import java.lang.reflect.Method

class MethodBytecodeVisitor(private val mv: MethodVisitor) {

  fun visitConstructorCall(fCall: ConstructorCallNode, argumentsPusher: () -> Unit) {
    val classInternalName = fCall.type.internalName
    mv.visitTypeInsn(Opcodes.NEW, classInternalName)
    mv.visitInsn(Opcodes.DUP)
    argumentsPusher.invoke()
    val constructorMethod = fCall.scope.getMethodForType(fCall.type, JavaMethod.CONSTRUCTOR_NAME, fCall.arguments)
    mv.visitMethodInsn(
      Opcodes.INVOKESPECIAL, classInternalName, fCall.name, constructorMethod.descriptor, false)
  }

  fun visitSuperConstructorCall(fCall: SuperConstructorCallNode, argumentsPusher: () -> Unit) {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    argumentsPusher.invoke()
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, fCall.scope.superClassInternalName, fCall.name,
      // void return type for constructors
      AsmUtils.getDescriptor(fCall.arguments, JavaType.void), false)
  }

  // arguments should be pushed before calling this method
  fun invokeMethod(method: Method) {
    invokeMethod(ReflectJavaMethod(method))
  }

  fun invokeMethod(method: JavaMethod) {
    // TODO when handling interfaces, might need to pass true sometimes
    mv.visitMethodInsn(method.invokeCode, method.ownerClass.internalName, method.name, method.descriptor, !method.isStatic && method.ownerClass.isInterface)
  }

  fun visitLabel(label: Label) {
    mv.visitLabel(label)
  }

  fun jumpIfEq(label: Label) {
    mv.visitJumpInsn(Opcodes.IFEQ, label)
  }


  fun jumpTo(label: Label) {
    mv.visitJumpInsn(Opcodes.GOTO, label)
  }

  // arguments should be pushed before calling this method
  fun not() {
    val endLabel = Label()
    val trueLabel = Label()
    mv.visitJumpInsn(Opcodes.IFEQ, trueLabel)
    mv.visitInsn(Opcodes.ICONST_0)
    mv.visitJumpInsn(Opcodes.GOTO, endLabel)
    mv.visitLabel(trueLabel)
    mv.visitInsn(Opcodes.ICONST_1)
    mv.visitLabel(endLabel)
  }
  fun popStack() {
    mv.visitInsn(Opcodes.POP)
  }

  fun pop2Stack() {
    mv.visitInsn(Opcodes.POP2)
  }

  fun pushConstant(boolean: Boolean) {
    mv.visitInsn(if (boolean) Opcodes.ICONST_1 else Opcodes.ICONST_0)
  }

  fun pushConstant(long: Long) {
    mv.visitLdcInsn(long)
  }

  fun pushConstant(integer: Int) {
    mv.visitLdcInsn(integer)
  }

  fun pushConstant(string: String) {
    mv.visitLdcInsn(string)
  }

  fun incr(incrNode: IncrNode) {
    mv.visitIincInsn(incrNode.variableReference.index, incrNode.amount)
  }

  fun returnVoid() {
    mv.visitInsn(Opcodes.RETURN)
  }

  fun pushNull() {
    mv.visitInsn(Opcodes.ACONST_NULL)
  }

  fun returnCode(opCode: Int) {
    mv.visitInsn(opCode)
  }

  fun comparisonJump(comparisonOperator: ComparisonOperator, label: Label) {
    mv.visitJumpInsn(comparisonOperator.iOpCode, label)
  }

  fun visitInsn(opCode: Int) {
    mv.visitInsn(opCode)
  }

  fun visitPrintlnCall(fCall: FunctionCallNode, argumentsPusher: () -> Unit) {
    mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")

    if (fCall.parameterTypes.size != 1) {
      throw SemanticException("Invalid call of println")
    }
    val argumentClass = fCall.arguments.first().type.realClassOrObject
    val method = PrintStream::class.java.getDeclaredMethod("println", if (argumentClass.isPrimitive) argumentClass else Object::class.java)
    argumentsPusher.invoke()
    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", AsmUtils.getDescriptor(method), false)
  }

  fun pushVariable(scope: Scope, variableName: String) {
    val (variable, index) = scope.getLocalVariableWithIndex(variableName)
    mv.visitVarInsn(variable.type.loadCode, index)
  }

  // must push expression before calling this method
  fun visitVariableAssignment(variableAssignmentNode: VariableAssignmentNode) {
    val (variable, index) = variableAssignmentNode.scope.getLocalVariableWithIndex(variableAssignmentNode.name)
    val variableType = variable.type
    val expressionType = variableAssignmentNode.expression.type
    if (variableType != expressionType) {
      if (variableType.primitive && expressionType.primitive) {
        val castInstruction = JavaType.PRIMITIVE_CAST_INSTRUCTION_MAP[Pair(expressionType, variableType)]
        if (castInstruction != null) {
          mv.visitInsn(castInstruction)
        } else {
          throw SemanticException("Cannot cast primitive $expressionType to primitive $variableType")
        }
      } else if (!variableType.primitive && !expressionType.primitive) {
        // both Object classses
        if (!variable.type.isAssignableFrom(variableAssignmentNode.expression.type)) {
          throw SemanticException("Incompatible types. Variable is of type ${variable.type} but gave an expression of type ${variableAssignmentNode.expression.type}")
        }
      } else {
        TODO("Doesn't handle primitive to Object and vice versa yet")
      }
    }
    mv.visitVarInsn(variable.type.storeCode, index)
  }

}