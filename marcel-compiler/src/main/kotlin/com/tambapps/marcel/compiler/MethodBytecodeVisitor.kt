package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.IncrNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.LocalVariable
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

  fun pushConstant(float: Float) {
    mv.visitLdcInsn(float)
  }

  fun pushConstant(double: Double) {
    mv.visitLdcInsn(double)
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
  fun castIfNecessaryOrThrow(variableType: JavaType, expressionType: JavaType) {
    if (variableType != expressionType) {
      if (variableType.primitive && expressionType.primitive) {
        val castInstruction = JavaType.PRIMITIVE_CAST_INSTRUCTION_MAP[Pair(expressionType, variableType)]
        if (castInstruction != null) {
          mv.visitInsn(castInstruction)
        } else {
          throw SemanticException("Cannot cast primitive $expressionType to primitive $variableType")
        }
      } else if (!variableType.primitive && !expressionType.primitive) {
        // both Object classes
        // TODO may need to cast sometimes
        if (!variableType.isAssignableFrom(expressionType)) {
          throw SemanticException("Incompatible types. Variable is of type $variableType but gave an expression of type $expressionType")
        }
      } else {
        if (variableType.primitive) {
          // cast Object to primitive
          when (variableType) {
            JavaType.int -> {
              if (variableType == JavaType.Integer) {
                throw SemanticException("Cannot cast $variableType to int")
              }
              invokeMethod(Class.forName(variableType.className).getMethod("intValue"))
            }
            JavaType.long -> {
              if (variableType == JavaType.Long) {
                throw SemanticException("Cannot cast $variableType to float")
              }
              invokeMethod(Class.forName(variableType.className).getMethod("longValue"))
            }
            JavaType.float -> {
              if (variableType == JavaType.Float) {
                throw SemanticException("Cannot cast $variableType to float")
              }
              invokeMethod(Class.forName(variableType.className).getMethod("floatValue"))
            }
            JavaType.double -> {
              if (variableType == JavaType.Double) {
                throw SemanticException("Cannot cast $variableType to float")
              }
              invokeMethod(Class.forName(variableType.className).getMethod("doubleValue"))
            }
            else -> throw SemanticException("Doesn't handle conversion from $expressionType to $variableType")
          }
        } else {
          // cast primitive to Object
          when (variableType) {
            JavaType.Integer -> {
              if (variableType == JavaType.int) {
                throw SemanticException("Cannot cast $variableType to Integer")
              }
              invokeMethod(Class.forName(variableType.className).getMethod("valueOf", Int::class.java))
            }
            JavaType.Long -> {
              if (variableType == JavaType.long) {
                throw SemanticException("Cannot cast $variableType to Long")
              }
              invokeMethod(Class.forName(variableType.className).getMethod("valueOf", Long::class.java))
            }
            JavaType.Float -> {
              if (variableType == JavaType.float) {
                throw SemanticException("Cannot cast $variableType to Float")
              }
              invokeMethod(Class.forName(variableType.className).getMethod("valueOf", Float::class.java))
            }
            JavaType.Double -> {
              if (variableType == JavaType.double) {
                throw SemanticException("Cannot cast $variableType to Double")
              }
              invokeMethod(Class.forName(variableType.className).getMethod("valueOf", Double::class.java))
            }
            else -> throw SemanticException("Doesn't handle conversion from $expressionType to $variableType")
          }
        }
      }
    }
  }

  fun storeInVariable(variable: LocalVariable, index: Int) {
    mv.visitVarInsn(variable.type.storeCode, index)
  }
}