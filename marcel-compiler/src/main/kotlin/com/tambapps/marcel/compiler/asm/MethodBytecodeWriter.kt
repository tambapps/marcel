package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.getType
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.AstNode
import com.tambapps.marcel.parser.ast.ComparisonOperator
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.LambdaNode
import com.tambapps.marcel.parser.ast.expression.NamedParametersConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
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

class MethodBytecodeWriter(private val mv: MethodVisitor, private val typeResolver: JavaTypeResolver) {

  lateinit var argumentPusher: ArgumentPusher

  fun visitNamedConstructorCall(fCall: NamedParametersConstructorCallNode) {
    val type = fCall.type
    if (typeResolver.findMethod(type, JavaMethod.CONSTRUCTOR_NAME, emptyList(), true) == null)
      throw MarcelSemanticException(fCall.token, "Cannot invoke named constructor on a type with no no-arg constructor")
    visitConstructorCall(ConstructorCallNode(fCall.token, fCall.scope, type, mutableListOf()))
    for (namedParameter in fCall.constructorNamedArguments) {
      dup()
      argumentPusher.pushArgument(namedParameter.valueExpression)
      val field = typeResolver.findFieldOrThrow(type, namedParameter.name)
      if (field.isFinal) throw MarcelSemanticException(fCall.token, "Cannot use named parameters constructor on a final field")
      castIfNecessaryOrThrow(namedParameter.valueExpression, field.type, namedParameter.valueExpression.getType(typeResolver))
      storeInVariable(fCall, field)
    }
  }

  fun visitConstructorCall(fCall: ConstructorCallNode) {
    val type = fCall.type
    val classInternalName = type.internalName
    mv.visitTypeInsn(Opcodes.NEW, classInternalName)
    mv.visitInsn(Opcodes.DUP)
    val constructorMethod = fCall.getMethod(typeResolver)
    pushFunctionCallArguments(fCall, constructorMethod, fCall.arguments)
    mv.visitMethodInsn(
      Opcodes.INVOKESPECIAL, classInternalName, JavaMethod.CONSTRUCTOR_NAME, constructorMethod.descriptor, false)
  }

  fun visitSuperConstructorCall(fCall: SuperConstructorCallNode) {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
    pushFunctionCallArguments(fCall, fCall.getMethod(typeResolver), fCall.arguments)
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, fCall.scope.superClass.internalName, fCall.name,
      // void return type for constructors
      AsmUtils.getDescriptor(fCall.arguments.map { it.getType(typeResolver) }, JavaType.void), false)
  }

  // arguments should be pushed before calling this method
  fun invokeMethod(from: AstNode, method: Method) {
    invokeMethod(from, ReflectJavaMethod(method))
  }

  fun invokeMethodWithArguments(from: AstNode, method: Method, vararg arguments: ExpressionNode) {
    invokeMethodWithArguments(from, ReflectJavaMethod(method), *arguments)
  }
  fun invokeMethodWithArguments(from: AstNode, method: JavaMethod, vararg arguments: ExpressionNode) {
    invokeMethodWithArguments(from, method, arguments.toList())
  }
  fun invokeMethodWithArguments(from: AstNode, method: JavaMethod, arguments: List<ExpressionNode>) {
    if (method.isConstructor) {
      throw RuntimeException("Compiler error. Shouldn't invoke constructor this way")
    }
    pushFunctionCallArguments(from, method, arguments)
    invokeMethod(from, method)
  }

  fun invokeMethod(from: AstNode, method: JavaMethod) {
    if (method.isConstructor) {
      throw RuntimeException("Compiler error. Shouldn't invoke constructor this way")
    }
    mv.visitMethodInsn(method.invokeCode, method.ownerClass.internalName, method.name, method.descriptor, !method.isStatic && method.ownerClass.isInterface)
    if (method.actualReturnType != method.returnType) {
      castIfNecessaryOrThrow(from, method.actualReturnType, method.returnType)
    }
  }
  private fun pushFunctionCallArguments(from: AstNode, method: JavaMethod, arguments: List<ExpressionNode>) {
    if (method.parameters.size != arguments.size) {
      throw MarcelSemanticException(from.token, "Tried to call function $method with ${arguments.size} instead of ${method.parameters.size}")
    }
    for (i in method.parameters.indices) {
      val expectedType = method.parameters[i].type
      val argument = arguments[i]
      if (argument is LambdaNode && expectedType.isInterface) {
        // TODO find a cleaner way to do this
        argument.interfaceType = expectedType
      }
      val actualType = argument.getType(typeResolver)
      argumentPusher.pushArgument(argument)
      castIfNecessaryOrThrow(from, expectedType, actualType)
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

  fun pushConstant(value: Short) {
    mv.visitIntInsn(Opcodes.SIPUSH, value.toInt())
  }

  fun pushConstant(value: Byte) {
    mv.visitIntInsn(Opcodes.BIPUSH, value.toInt())
  }

  fun pushConstant(string: String) {
    mv.visitLdcInsn(string)
  }

  fun pushConstant(char: Char) {
    mv.visitLdcInsn(char)
  }

  fun incrLocalVariable(variable: LocalVariable, amount: Int) {
    mv.visitIincInsn(variable.index, amount)
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
  fun jump(opCode: Int, label: Label) {
    mv.visitJumpInsn(opCode, label)
  }

  fun visitInsn(opCode: Int) {
    mv.visitInsn(opCode)
  }

  fun pushThis() {
    mv.visitVarInsn(Opcodes.ALOAD, 0)
  }

  fun pushVariable(from: AstNode, scope: Scope, variable: Variable) {
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
            throw MarcelSemanticException(from.token, "Variable ${variable.name} has no getter")
          }
          invokeMethod(from, variable.getterMethod)
        }
      }
      else -> throw MarcelSemanticException(from.token, "Variable type ${variable.javaClass} is not handled")
    }
  }

  // must push expression before calling this method
  fun castIfNecessaryOrThrow(from: AstNode, expectedType: JavaType, actualType: JavaType) {
    if (expectedType != actualType) {
      if (expectedType.primitive && actualType.primitive) {
        val castInstruction = JavaType.PRIMITIVE_CAST_INSTRUCTION_MAP[Pair(actualType, expectedType)]
        if (castInstruction != null) {
          mv.visitInsn(castInstruction)
        } else if (expectedType == JavaType.char && actualType == JavaType.int) { // no need to cast for char to int conversion
          throw MarcelSemanticException(from.token, "Cannot cast primitive $actualType to primitive $expectedType")
        }
      } else if (expectedType != JavaType.Object && actualType.isArray) {
        // lists
        if (JavaType.intList.isAssignableFrom(expectedType) && actualType == JavaType.intArray) {
          invokeMethod(from, typeResolver.findMethodOrThrow(JavaType.intListImpl, "wrap", listOf(JavaType.intArray)))
        } else if (JavaType.longList.isAssignableFrom(expectedType) && actualType == JavaType.longArray) {
          invokeMethod(from, typeResolver.findMethodOrThrow(JavaType.longListImpl, "wrap", listOf(JavaType.longArray)))
        } else if (JavaType.floatList.isAssignableFrom(expectedType) && actualType == JavaType.floatArray) {
          invokeMethod(from, typeResolver.findMethodOrThrow(JavaType.floatListImpl, "wrap", listOf(JavaType.floatArray)))
        } else if (JavaType.doubleList.isAssignableFrom(expectedType) && actualType == JavaType.doubleArray) {
          invokeMethod(from, typeResolver.findMethodOrThrow(JavaType.doubleListImpl, "wrap", listOf(JavaType.doubleArray)))
        } else if (JavaType.charList.isAssignableFrom(expectedType) && actualType == JavaType.charArray) {
          invokeMethod(from, typeResolver.findMethodOrThrow(JavaType.charListImpl, "wrap", listOf(JavaType.charArray)))
        } else if (List::class.javaType.isAssignableFrom(expectedType) && actualType.isArray) {
          invokeMethod(from, BytecodeHelper::class.java.getDeclaredMethod("createList", JavaType.Object.realClazz))
        }
        // sets
        else if (JavaType.intSet.isAssignableFrom(expectedType) && actualType == JavaType.intArray) {
          invokeMethod(from, BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.intArray.realClazz))
        } else if (JavaType.longSet.isAssignableFrom(expectedType) && actualType == JavaType.longArray) {
          invokeMethod(from, BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.longArray.realClazz))
        } else if (JavaType.floatSet.isAssignableFrom(expectedType) && actualType == JavaType.floatArray) {
          invokeMethod(from, BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.floatArray.realClazz))
        } else if (JavaType.doubleSet.isAssignableFrom(expectedType) && actualType == JavaType.doubleArray) {
          invokeMethod(from, BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.doubleArray.realClazz))
        } else if (JavaType.characterSet.isAssignableFrom(expectedType) && actualType == JavaType.charArray) {
          invokeMethod(from, BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.charArray.realClazz))
        } else if (Set::class.javaType.isAssignableFrom(expectedType) && actualType.isArray) {
          invokeMethod(from, BytecodeHelper::class.java.getDeclaredMethod("createSet", JavaType.Object.realClazz))
        } else {
          throw MarcelSemanticException(from.token, "Incompatible types. Expected type $expectedType but gave an expression of type $actualType")
        }
      } else if (!expectedType.primitive && !actualType.primitive) {
        // both Object classes
        if (!expectedType.isAssignableFrom(actualType)) {
          if ((expectedType == JavaType.Character || expectedType == JavaType.char) && JavaType.of(CharSequence::class.java).isAssignableFrom(actualType)) {
            // get the first char of the string
            pushConstant(0)
            invokeMethod(from, String::class.java.getMethod("charAt", Int::class.java))
            if (expectedType == JavaType.Character) {
              invokeMethod(from, Character::class.java.getMethod("valueOf", Char::class.java))
            }
          } else if (actualType.isAssignableFrom(expectedType)) {
            // actualType is a parent of expectedType? might be able to cast it
            mv.visitTypeInsn(Opcodes.CHECKCAST, expectedType.internalName)
          } else {
            throw MarcelSemanticException(from.token, "Incompatible types. Expected type $expectedType but gave an expression of type $actualType")
          }
        }
      } else {
        if (expectedType.primitive) {
          // cast Object to primitive
          when (expectedType) {
            JavaType.boolean -> {
              if (actualType != JavaType.Boolean) {
                // try to cast Object to Boolean
                castIfNecessaryOrThrow(from, JavaType.Boolean, actualType)
              }
              invokeMethod(from, Class.forName(JavaType.Boolean.className).getMethod("booleanValue"))
            }
            JavaType.int -> {
              if (actualType != JavaType.Integer) {
                // try to cast Object to Integer
                castIfNecessaryOrThrow(from, JavaType.Integer, actualType)
              }
              invokeMethod(from, Class.forName(JavaType.Integer.className).getMethod("intValue"))
            }
            JavaType.char -> {
              if (actualType == JavaType.String) {
                // get the first char of the string
                pushConstant(0)
                invokeMethod(from, String::class.java.getMethod("charAt", Int::class.java))
                return
              } else if (actualType != JavaType.Character) {
                // try to cast Object to Character
                castIfNecessaryOrThrow(from, JavaType.Character, actualType)
              }
              invokeMethod(from, Class.forName(JavaType.Character.className).getMethod("charValue"))
            }
            JavaType.long -> {
              if (actualType != JavaType.Long) {
                // try to cast Object to Long
                castIfNecessaryOrThrow(from, JavaType.Long, actualType)
              }
              invokeMethod(from, Class.forName(JavaType.Long.className).getMethod("longValue"))
            }
            JavaType.float -> {
              if (actualType != JavaType.Float) {
                // try to cast Object to Float
                castIfNecessaryOrThrow(from, JavaType.Float, actualType)
              }
              invokeMethod(from, Class.forName(JavaType.Float.className).getMethod("floatValue"))
            }
            JavaType.double -> {
              if (actualType != JavaType.Double) {
                // try to cast Object to Double
                castIfNecessaryOrThrow(from, JavaType.Double, actualType)
              }
              invokeMethod(from, Class.forName(JavaType.Double.className).getMethod("doubleValue"))
            }
            else -> throw MarcelSemanticException(from.token, "Doesn't handle conversion from $actualType to $expectedType")
          }
        } else {
          // cast primitive to Object
          if (expectedType == JavaType.Boolean && actualType != JavaType.boolean
            || expectedType == JavaType.Integer && actualType != JavaType.int
            || expectedType == JavaType.Long && actualType != JavaType.long
            || expectedType == JavaType.Float && actualType != JavaType.float
            || expectedType == JavaType.Double && actualType != JavaType.double
            || expectedType == JavaType.Character && actualType != JavaType.char
            || expectedType !in listOf(
              JavaType.Boolean, JavaType.Integer, JavaType.Long, JavaType.Float, JavaType.Double, JavaType.Character, Number::class.javaType, JavaType.Object
            )) {
            throw MarcelSemanticException(from.token, "Cannot cast $actualType to $expectedType")
          }
          when (actualType) {
            JavaType.boolean -> invokeMethod(from, Class.forName(JavaType.Boolean.className).getMethod("valueOf", Boolean::class.java))
            JavaType.int -> invokeMethod(from, Class.forName(JavaType.Integer.className).getMethod("valueOf", Int::class.java))
            JavaType.long -> invokeMethod(from, Class.forName(JavaType.Long.className).getMethod("valueOf", Long::class.java))
            JavaType.float -> invokeMethod(from, Class.forName(JavaType.Float.className).getMethod("valueOf", Float::class.java))
            JavaType.double -> invokeMethod(from, Class.forName(JavaType.Double.className).getMethod("valueOf", Double::class.java))
            JavaType.char -> invokeMethod(from, Class.forName(JavaType.Character.className).getMethod("valueOf", JavaType.char.realClazz))
            else -> throw MarcelSemanticException(from.token, "Doesn't handle conversion from $actualType to $expectedType")
          }
        }
      }
    }
  }

  fun storeInVariable(from: AstNode, variable: Variable) {
    if (variable.isFinal && variable.alreadySet) throw MarcelSemanticException(from.token, "Cannot reset a value for final variable ${variable.name}")
    when (variable) {
      is LocalVariable -> mv.visitVarInsn(variable.type.storeCode, variable.index)

      // TODO need to check visibility too
      // for fields, the caller should push the field's owner
      is ClassField -> mv.visitFieldInsn(variable.putCode, variable.owner.internalName, variable.name, variable.type.descriptor)
      is MethodField -> {
        if (!variable.canSet) {
          throw MarcelSemanticException(from.token, "Field ${variable.name} of class ${variable.owner} is not settable")
        }
        invokeMethod(from, variable.setterMethod)
      }
      else -> throw RuntimeException("Compiler bug. Not handled variable subclass ${variable.javaClass}")
    }
    variable.alreadySet = true
  }

  fun getField(from: AstNode, field: MarcelField) {
    when (field) {
      is ClassField -> {
        mv.visitFieldInsn(field.getCode, field.owner.internalName, field.name, field.type.descriptor)
      }
      is MethodField -> {
        if (!field.canGet) {
          throw MarcelSemanticException(from.token, "Field ${field.name} of class ${field.owner} is not gettable")
        }
        invokeMethod(from, field.getterMethod)
      }
      else -> throw RuntimeException("Compiler bug. Not handled field subclass ${field.javaClass}")
    }
  }

  fun newArray(from: AstNode, type: JavaArrayType, elements: List<ExpressionNode>) {
    // Push the size of the array to the stack
    pushConstant(elements.size)
    // Create an int array of size n
    if (type.elementsType.primitive) {
      mv.visitIntInsn(Opcodes.NEWARRAY, type.typeCode)
    } else {
      mv.visitTypeInsn(Opcodes.ANEWARRAY, type.elementsType.internalName)
    }

    for (i in elements.indices) {
      // Push the array reference on the stack
      mv.visitInsn(Opcodes.DUP)
      // push the index
      pushConstant(i)
      // push the value
      argumentPusher.pushArgument(elements[i])
      castIfNecessaryOrThrow(from, type.elementsType, elements[i].getType(typeResolver))
      // store value at index
      mv.visitInsn(type.arrayStoreCode)
    }
  }

  fun pushVariableGetAt(from: AstNode, scope: Scope, variable: Variable, indexArguments: List<ExpressionNode>) {
    val variableType = variable.type
    // push array
    pushVariable(from, scope, variable)
    getAt(from, variableType, indexArguments)
  }
  fun getAt(from: AstNode, type: JavaType, indexArguments: List<ExpressionNode>) {
    if (type.isArray) {
      if (indexArguments.size != 1) throw MarcelSemanticException(from.token, "Need only one int argument to get an array")
      val arg = indexArguments.first()
      // push index
      argumentPusher.pushArgument(arg)
      // cast if necessary (e.g. Integer to int)
      castIfNecessaryOrThrow(from, JavaType.int, arg.getType(typeResolver))
      // load value in pushed array int pushed index
      mv.visitInsn(type.asArrayType.arrayLoadCode)
    } else {
      // must call getAt
      invokeMethodWithArguments(from, typeResolver.findMethodOrThrow(type, "getAt", indexArguments.map { it.getType(typeResolver) }), indexArguments)
    }
  }

  fun storeInVariablePutAt(
    from: AstNode,
    scope: Scope,
    variable: Variable,
    indexArguments: List<ExpressionNode>,
    expression: ExpressionNode
  ) {
    if (variable.type.isArray) {
      val variableType = variable.type.asArrayType
      if (indexArguments.size != 1) throw MarcelSemanticException(from.token, "Need only one int argument to get an array")
      val arg = indexArguments.first()
      // push array
      pushVariable(from, scope, variable)
      // push index
      argumentPusher.pushArgument(arg)
      // cast if necessary (e.g. Integer to int)
      castIfNecessaryOrThrow(from, JavaType.int, arg.getType(typeResolver))
      // push value to set
      argumentPusher.pushArgument(expression)
      castIfNecessaryOrThrow(from, variableType.elementsType, expression.getType(typeResolver))

      // load/store value in pushed array int pushed index
      mv.visitInsn(variableType.arrayStoreCode)
    } else {
      // must call putAt
      pushVariable(from, scope, variable)
      val putAtArguments = indexArguments + expression
      invokeMethodWithArguments(from, typeResolver.findMethodOrThrow(variable.type, "putAt", putAtArguments.map { it.getType(typeResolver) }), putAtArguments)
    }
  }

  fun tryFinallyBlock(startLabel: Label, endLabel: Label, catchLabel: Label) {
    mv.visitTryCatchBlock(startLabel, endLabel, catchLabel, null)
  }
  fun tryCatchBlock(startLabel: Label, endLabel: Label, catchLabel: Label, javaType: JavaType) {
    mv.visitTryCatchBlock(startLabel, endLabel, catchLabel, javaType.internalName)
  }

  fun catchBlock(label: Label, exceptionVarIndex: Int) {
    mv.visitLabel(label)
    mv.visitVarInsn(Opcodes.ASTORE, exceptionVarIndex)
  }

  fun finallyBlock(label: Label) {
    mv.visitLabel(label)
   // popStack() // popping stack  because we don't care about the exception variable in the finally block
  }
  fun foo(i: Int) {
    mv.visitVarInsn(Opcodes.ALOAD, i)
    mv.visitInsn(Opcodes.ATHROW)
  }
}