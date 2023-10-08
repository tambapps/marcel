package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.loadCode
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.VariableVisitor
import com.tambapps.marcel.semantic.variable.field.BoundField
import com.tambapps.marcel.semantic.variable.field.CompositeField
import com.tambapps.marcel.semantic.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.variable.field.JavaClassField
import com.tambapps.marcel.semantic.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.variable.field.MethodField
import org.objectweb.asm.MethodVisitor

/**
 * Visitor loading variables on the stack
 */
class LoadVariableVisitor(
  private val mv: MethodVisitor
) : VariableVisitor<Unit> {

  override fun visit(variable: LocalVariable) = mv.visitVarInsn(variable.type.loadCode, variable.index)

  override fun visit(variable: BoundField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: DynamicMethodField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: JavaClassField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: MarcelArrayLengthField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: CompositeField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: MethodField) {
    TODO("Not yet implemented")
  }
}