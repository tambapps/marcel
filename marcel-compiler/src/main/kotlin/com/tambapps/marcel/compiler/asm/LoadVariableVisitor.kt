package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.loadCode
import com.tambapps.marcel.compiler.extensions.visitMethodInsn
import com.tambapps.marcel.semantic.method.ReflectJavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.VariableVisitor
import com.tambapps.marcel.semantic.variable.field.BoundField
import com.tambapps.marcel.semantic.variable.field.CompositeField
import com.tambapps.marcel.semantic.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.variable.field.JavaClassField
import com.tambapps.marcel.semantic.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.variable.field.MethodField
import marcel.lang.Script
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Visitor loading variables on the stack
 */
class LoadVariableVisitor(
  private val mv: MethodVisitor,
  private val classScopeType: JavaType
) : VariableVisitor<Unit> {

  private companion object {
    val GET_VARIABLE_METHOD = ReflectJavaMethod(
      Script::class.java.getMethod("getVariable", String::class.java)
    )
  }
  override fun visit(variable: LocalVariable) = mv.visitVarInsn(variable.type.loadCode, variable.index)

  override fun visit(variable: BoundField) {
    mv.visitLdcInsn(variable.name)
    mv.visitMethodInsn(GET_VARIABLE_METHOD)
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
    mv.visitMethodInsn(javaMethod)
  }
}