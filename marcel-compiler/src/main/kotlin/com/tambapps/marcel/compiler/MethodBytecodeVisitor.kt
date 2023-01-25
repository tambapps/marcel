package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.*
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.reflect.Method

class MethodBytecodeVisitor(private val mv: MethodVisitor) {

  fun visitConstructorCall(fCall: ConstructorCallNode, argumentsPusher: () -> Unit) {
    val classInternalName = fCall.type.internalName
    mv.visitTypeInsn(Opcodes.NEW, classInternalName)
    mv.visitInsn(Opcodes.DUP)
    argumentsPusher.invoke()
    val constructorMethod = fCall.type.findDeclaredConstructorOrThrow(fCall.arguments)
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

  fun incr(variable: Variable, amount: Int) {
    when (variable) {
      is LocalVariable -> mv.visitIincInsn(variable.index, amount)
      else -> throw TODO("")
    }
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

  fun jumpIfNe(label: Label) {
    mv.visitJumpInsn(Opcodes.IFNE, label)
  }
  fun comparisonJump(comparisonOperator: ComparisonOperator, label: Label) {
    mv.visitJumpInsn(comparisonOperator.iOpCode, label)
  }

  fun visitInsn(opCode: Int) {
    mv.visitInsn(opCode)
  }

  fun pushVariable(variable: Variable) {
    when (variable) {
      is LocalVariable -> mv.visitVarInsn(variable.type.loadCode, variable.index)
      else -> TODO("TODO")
    }
  }

  // must push expression before calling this method
  fun castIfNecessaryOrThrow(expectedType: JavaType, actualType: JavaType) {
    if (expectedType != actualType) {
      if (expectedType.primitive && actualType.primitive) {
        val castInstruction = JavaType.PRIMITIVE_CAST_INSTRUCTION_MAP[Pair(actualType, expectedType)]
        if (castInstruction != null) {
          mv.visitInsn(castInstruction)
        } else {
          throw SemanticException("Cannot cast primitive $actualType to primitive $expectedType")
        }
      } else if (!expectedType.primitive && !actualType.primitive) {
        // both Object classes
        if (!expectedType.isAssignableFrom(actualType)) {
          if (actualType.isAssignableFrom(expectedType)) {
            // actualType is a parent of expectedType? might be able to cast it
            mv.visitTypeInsn(Opcodes.CHECKCAST, expectedType.internalName)
          } else {
            throw SemanticException("Incompatible types. Expected type $expectedType but gave an expression of type $actualType")
          }
        }
      } else {
        if (expectedType.primitive) {
          // cast Object to primitive
          when (expectedType) {
            JavaType.boolean -> {
              if (actualType != JavaType.Boolean) {
                throw SemanticException("Cannot cast $actualType to boolean")
              }
              invokeMethod(Class.forName(JavaType.Boolean.className).getMethod("booleanValue"))
            }
            JavaType.int -> {
              if (actualType != JavaType.Integer) {
                throw SemanticException("Cannot cast $actualType to int")
              }
              invokeMethod(Class.forName(JavaType.Integer.className).getMethod("intValue"))
            }
            JavaType.long -> {
              if (actualType != JavaType.Long) {
                throw SemanticException("Cannot cast $actualType to float")
              }
              invokeMethod(Class.forName(JavaType.Long.className).getMethod("longValue"))
            }
            JavaType.float -> {
              if (actualType != JavaType.Float) {
                throw SemanticException("Cannot cast $actualType to float")
              }
              invokeMethod(Class.forName(JavaType.Float.className).getMethod("floatValue"))
            }
            JavaType.double -> {
              if (actualType != JavaType.Double) {
                throw SemanticException("Cannot cast $actualType to float")
              }
              invokeMethod(Class.forName(JavaType.Double.className).getMethod("doubleValue"))
            }
            else -> throw SemanticException("Doesn't handle conversion from $actualType to $expectedType")
          }
        } else {
          // cast primitive to Object
          if (expectedType == JavaType.Boolean && actualType != JavaType.boolean
            || expectedType == JavaType.Integer && actualType != JavaType.int
            || expectedType == JavaType.Long && actualType != JavaType.long
            || expectedType == JavaType.Float && actualType != JavaType.float
            || expectedType == JavaType.Double && actualType != JavaType.double
            || expectedType !in listOf(
              JavaType.Boolean, JavaType.Integer, JavaType.Long, JavaType.Float, JavaType.Double, JavaType.of(Number::class.java), JavaType.Object
            )) {
            throw SemanticException("Cannot cast $actualType to $actualType")
          }
          when (actualType) {
            JavaType.boolean -> invokeMethod(Class.forName(JavaType.Boolean.className).getMethod("valueOf", Boolean::class.java))
            JavaType.int -> invokeMethod(Class.forName(JavaType.Integer.className).getMethod("valueOf", Int::class.java))
            JavaType.long -> invokeMethod(Class.forName(JavaType.Long.className).getMethod("valueOf", Long::class.java))
            JavaType.float -> invokeMethod(Class.forName(JavaType.Float.className).getMethod("valueOf", Float::class.java))
            JavaType.double -> invokeMethod(Class.forName(JavaType.Double.className).getMethod("valueOf", Double::class.java))
            else -> throw SemanticException("Doesn't handle conversion from $actualType to $expectedType")
          }
        }
      }
    }
  }

  fun storeInVariable(variable: Variable) {
    when (variable) {
      is LocalVariable -> mv.visitVarInsn(variable.type.storeCode, variable.index)
      else -> throw TODO("todo")
    }
  }

  fun getField(field: MarcelField) {
    when (field) {
      is ClassField -> {
        mv.visitFieldInsn(field.getCode, field.owner.internalName, field.name, field.type.descriptor)
      }
      is MethodField -> {
        if (!field.canGet) {
          throw SemanticException("Field ${field.name} of class ${field.owner} is not gettable")
        }
        mv.visitMethodInsn(field.invokeCode, field.owner.internalName, field.getterName, AsmUtils.getDescriptor(emptyList(), field.type), field.type.isInterface)
      }
      else -> {
        throw RuntimeException("Compiler bug. Unknown field subclass ${field.javaClass}")
      }
    }
  }
}