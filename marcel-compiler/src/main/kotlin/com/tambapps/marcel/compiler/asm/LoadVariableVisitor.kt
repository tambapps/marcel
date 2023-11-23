package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.invokeCode
import com.tambapps.marcel.compiler.extensions.loadCode
import com.tambapps.marcel.compiler.extensions.visitMethodInsn
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
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
  private val typeResolver: JavaTypeResolver,
  private val mv: MethodVisitor,
  private val classScopeType: JavaType
) : VariableVisitor<Unit> {

  override fun visit(variable: LocalVariable) = mv.visitVarInsn(variable.type.loadCode, variable.index)

  override fun visit(variable: BoundField) {
    mv.visitLdcInsn(variable.name)
    mv.visitMethodInsn(typeResolver.findMethodOrThrow(Script::class.javaType, "getVariable", listOf(JavaType.String)))
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