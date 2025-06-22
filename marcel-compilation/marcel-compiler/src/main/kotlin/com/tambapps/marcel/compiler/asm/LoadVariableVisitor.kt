package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.loadCode
import com.tambapps.marcel.semantic.symbol.method.ReflectJavaMethod
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.VariableVisitor
import com.tambapps.marcel.semantic.symbol.variable.field.BoundField
import com.tambapps.marcel.semantic.symbol.variable.field.CompositeField
import com.tambapps.marcel.semantic.symbol.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.symbol.variable.field.JavaClassField
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.symbol.variable.field.MethodField
import marcel.lang.Script
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Visitor loading variables on the stack
 */
class LoadVariableVisitor(
  private val mv: MethodVisitor,
  private val classScopeType: JavaType,
  private val methodCallWriter: MethodCallWriter
) : VariableVisitor<Unit> {

  private companion object {
    val GET_VARIABLE_METHOD = ReflectJavaMethod(
      Script::class.java.getMethod("getVariable", String::class.java)
    )
  }
  override fun visit(variable: LocalVariable) = mv.visitVarInsn(variable.type.loadCode, variable.index)

  override fun visit(variable: BoundField) {
    mv.visitLdcInsn(variable.name)
    GET_VARIABLE_METHOD.accept(methodCallWriter)
    // bound variable type should always be an object, so no need to check primitive from/to object casting. this will always be object to object
    mv.visitTypeInsn(Opcodes.CHECKCAST, variable.type.internalName)
  }

  override fun visit(variable: DynamicMethodField) {
    // need to push name
    mv.visitLdcInsn(variable.name)
    visit(variable as MethodField)
  }

  override fun visit(variable: JavaClassField) = mv.visitFieldInsn(if (variable.isStatic) Opcodes.GETSTATIC else Opcodes.GETFIELD, variable.owner.internalName, variable.name, variable.type.descriptor)

  override fun visit(variable: MarcelArrayLengthField) = mv.visitInsn(Opcodes.ARRAYLENGTH)

  override fun visit(variable: CompositeField) {
    val field = variable.gettableFieldFrom(classScopeType) ?: throw RuntimeException("Compiler error.")
    field.accept(this)
  }

  override fun visit(variable: MethodField) {
    val javaMethod = variable.getterMethod
    javaMethod.accept(methodCallWriter)
  }
}