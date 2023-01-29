package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.exception.SemanticException
import com.tambapps.marcel.parser.scope.*
import com.tambapps.marcel.parser.type.JavaArrayType
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import com.tambapps.marcel.parser.type.ReflectJavaMethod
import marcel.lang.runtime.BytecodeHelper
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.lang.reflect.Method

class MethodBytecodeVisitor(private val mv: MethodVisitor) {

  lateinit var argumentPusher: ArgumentPusher

  fun visitConstructorCall(fCall: ConstructorCallNode) {
    val type = fCall.type
    val classInternalName = type.internalName
    mv.visitTypeInsn(Opcodes.NEW, classInternalName)
    mv.visitInsn(Opcodes.DUP)
    val constructorMethod = fCall.method
    pushFunctionCallArguments(constructorMethod, fCall.arguments)
    mv.visitMethodInsn(
      Opcodes.INVOKESPECIAL, classInternalName, JavaMethod.CONSTRUCTOR_NAME, constructorMethod.descriptor, false)
  }

  fun visitSuperConstructorCall(fCall: SuperConstructorCallNode) {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    pushFunctionCallArguments(fCall.method, fCall.arguments)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, fCall.scope.superClassInternalName, fCall.name,
      // void return type for constructors
      AsmUtils.getDescriptor(fCall.arguments, JavaType.void), false)
  }

  // arguments should be pushed before calling this method
  fun invokeMethod(method: Method) {
    invokeMethod(ReflectJavaMethod(method))
  }

  fun invokeMethod(method: JavaMethod) {
    if (method.isConstructor) {
      throw RuntimeException("Compiler error. Shouldn't invoke constructor this way")
    }
    mv.visitMethodInsn(method.invokeCode, method.ownerClass.internalName, method.name, method.descriptor, !method.isStatic && method.ownerClass.isInterface)
  }

  fun invokeMethodWithArguments(method: Method, vararg arguments: ExpressionNode) {
    invokeMethodWithArguments(ReflectJavaMethod(method), *arguments)
  }
  fun invokeMethodWithArguments(method: JavaMethod, vararg arguments: ExpressionNode) {
    invokeMethodWithArguments(method, arguments.toList())
  }
  fun invokeMethodWithArguments(method: JavaMethod, arguments: List<ExpressionNode>) {
    if (method.isConstructor) {
      throw RuntimeException("Compiler error. Shouldn't invoke constructor this way")
    }
    pushFunctionCallArguments(method, arguments)
    mv.visitMethodInsn(method.invokeCode, method.ownerClass.internalName, method.name, method.descriptor, !method.isStatic && method.ownerClass.isInterface)
  }
  private fun pushFunctionCallArguments(method: JavaMethod, arguments: List<ExpressionNode>) {
    if (method.parameters.size != arguments.size) {
      throw SemanticException("Tried to call function $method with ${arguments.size} instead of ${method.parameters.size}")
    }
    for (i in method.parameters.indices) {
      val expectedType = method.parameters[i].type
      val actualType = arguments[i].type
      argumentPusher.pushArgument(arguments[i])
      castIfNecessaryOrThrow(expectedType, actualType)
    }
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
      else -> throw TODO("Doesn't handle increment of other kind")
    }
  }

  fun returnVoid() {
    mv.visitInsn(Opcodes.RETURN)
  }

  fun pushNull() {
    mv.visitInsn(Opcodes.ACONST_NULL)
  }


  fun dup() {
    mv.visitInsn(Opcodes.DUP)
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

  fun pushThis() {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
  }

  fun pushVariable(scope: Scope, variable: Variable) {
    when (variable) {
      is LocalVariable -> mv.visitVarInsn(variable.type.loadCode, variable.index)
      is ClassField ->   {
        if (!variable.isStatic) {
          if (variable.owner.isAssignableFrom(scope.classType)) {
            pushThis()
          } else {
            throw RuntimeException("Compiler error. Shouldn't push class field of not current class with this method")
          }
        }
        mv.visitFieldInsn(variable.getCode, variable.owner.internalName, variable.name, variable.type.descriptor)
      }
      is MethodField -> {
        if (!variable.isStatic) {
          if (variable.owner.isAssignableFrom(scope.classType)) {
            pushThis()
          } else {
            throw RuntimeException("Compiler error. Shouldn't push class field of not current class with this method")
          }
          if (!variable.canGet) {
            throw SemanticException("Variable ${variable.name} has no getter")
          }
          invokeMethod(variable.getterMethod)
        }
      }
      else -> throw SemanticException("Variable type ${variable.javaClass} is not handled")
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
      } else if (expectedType != JavaType.Object && actualType.isArray) {
        // lists
        if (JavaType.intList.isAssignableFrom(expectedType) && actualType == JavaType.intArray) {
          invokeMethod(JavaType.intListImpl.findMethodOrThrow("wrap", listOf(JavaType.intArray)))
        } else if (JavaType.longList.isAssignableFrom(expectedType) && actualType == JavaType.longArray) {
          invokeMethod(JavaType.longListImpl.findMethodOrThrow("wrap", listOf(JavaType.longArray)))
        } else if (JavaType.floatList.isAssignableFrom(expectedType) && actualType == JavaType.floatArray) {
          invokeMethod(JavaType.floatListImpl.findMethodOrThrow("wrap", listOf(JavaType.floatArray)))
        } else if (JavaType.doubleList.isAssignableFrom(expectedType) && actualType == JavaType.doubleArray) {
          invokeMethod(JavaType.doubleListImpl.findMethodOrThrow("wrap", listOf(JavaType.doubleArray)))
        } else if (JavaType.booleanList.isAssignableFrom(expectedType) && actualType == JavaType.booleanArray) {
          invokeMethod(JavaType.booleanListImpl.findMethodOrThrow("wrap", listOf(JavaType.booleanArray)))
        } else if (JavaType.of(List::class.java).isAssignableFrom(expectedType) && actualType.isArray) {
          invokeMethod(BytecodeHelper::class.java.getDeclaredMethod("createList", JavaType.Object.realClazz))
        }
        // sets
        else if (JavaType.intSet.isAssignableFrom(expectedType) && actualType == JavaType.intArray) {
          invokeMethod(BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.intArray.realClazz))
        } else if (JavaType.longSet.isAssignableFrom(expectedType) && actualType == JavaType.longArray) {
          invokeMethod(BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.longArray.realClazz))
        } else if (JavaType.floatSet.isAssignableFrom(expectedType) && actualType == JavaType.floatArray) {
          invokeMethod(BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.floatArray.realClazz))
        } else if (JavaType.doubleSet.isAssignableFrom(expectedType) && actualType == JavaType.doubleArray) {
          invokeMethod(BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.doubleArray.realClazz))
        } else if (JavaType.booleanSet.isAssignableFrom(expectedType) && actualType == JavaType.booleanArray) {
          invokeMethod(BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.booleanArray.realClazz))
        } else if (JavaType.of(Set::class.java).isAssignableFrom(expectedType) && actualType.isArray) {
          invokeMethod(BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.Object.realClazz))
        } else {
          throw SemanticException("Incompatible types. Expected type $expectedType but gave an expression of type $actualType")
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
            throw SemanticException("Cannot cast $actualType to $expectedType")
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
      is ClassField -> mv.visitFieldInsn(variable.putCode, variable.owner.internalName, variable.name, variable.type.descriptor)
      is MethodField -> {
        if (!variable.canSet) {
          throw SemanticException("Field ${variable.name} of class ${variable.owner} is not settable")
        }
        invokeMethod(variable.setterMethod)
      }
      else -> throw RuntimeException("Compiler bug. Not handled variable subclass ${variable.javaClass}")
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
        invokeMethod(field.getterMethod)
      }
      else -> throw RuntimeException("Compiler bug. Not handled field subclass ${field.javaClass}")
    }
  }

  fun newArray(type: JavaArrayType, elements: List<ExpressionNode>) {
    // Push the size of the array to the stack
    pushConstant(elements.size)
    // Create an int array of size n
    if (type.elementsType.primitive) {
      mv.visitIntInsn(Opcodes.NEWARRAY, type.typeCode)
    } else {
      // TODO Object[] arrays don's seem to work
      mv.visitTypeInsn(Opcodes.ANEWARRAY, type.elementsType.internalName)
    }

    for (i in elements.indices) {
      // Push the array reference on the stack
      mv.visitInsn(Opcodes.DUP)
      // push the index
      pushConstant(i)
      // push the value
      argumentPusher.pushArgument(elements[i])
      castIfNecessaryOrThrow(type.elementsType, elements[i].type)
      // store value at index
      mv.visitInsn(type.arrayStoreCode)
    }
  }

  fun pushVariableGetAt(scope: Scope, variable: Variable, indexArguments: List<ExpressionNode>) {
    val variableType = variable.type
    if (variableType is JavaArrayType) {
      if (indexArguments.size != 1) throw SemanticException("Need only one int argument to get an array")
      val arg = indexArguments.first()
      // push array
      pushVariable(scope, variable)
      // push index
      argumentPusher.pushArgument(arg)
      // cast if necessary (e.g. Integer to int)
      castIfNecessaryOrThrow(JavaType.int, arg.type)
      // load value in pushed array int pushed index
      mv.visitInsn(variableType.arrayLoadCode)
    } else {
      // must call getAt
      pushVariable(scope, variable)
      invokeMethodWithArguments(variableType.findMethodOrThrow("getAt", indexArguments), indexArguments)
    }
  }

  fun storeInVariablePutAt(
    scope: Scope,
    variable: Variable,
    indexArguments: List<ExpressionNode>,
    expression: ExpressionNode
  ) {
    if (variable.type is JavaArrayType) {
      val variableType = variable.type as JavaArrayType
      if (indexArguments.size != 1) throw SemanticException("Need only one int argument to get an array")
      val arg = indexArguments.first()
      // push array
      pushVariable(scope, variable)
      // push index
      argumentPusher.pushArgument(arg)
      // cast if necessary (e.g. Integer to int)
      castIfNecessaryOrThrow(JavaType.int, arg.type)
      // push value to set
      argumentPusher.pushArgument(expression)
      castIfNecessaryOrThrow(variableType.elementsType, expression.type)

      // load/store value in pushed array int pushed index
      mv.visitInsn(variableType.arrayStoreCode)
    } else {
      // must call putAt
      pushVariable(scope, variable)
      val putAtArguments = indexArguments + expression
      invokeMethodWithArguments(variable.type.findMethodOrThrow("putAt", putAtArguments), putAtArguments)
    }
  }
}