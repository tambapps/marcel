package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.invokeCode
import com.tambapps.marcel.compiler.extensions.storeCode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.VariableVisitor
import com.tambapps.marcel.semantic.variable.field.BoundField
import com.tambapps.marcel.semantic.variable.field.CompositeField
import com.tambapps.marcel.semantic.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.variable.field.JavaClassField
import com.tambapps.marcel.semantic.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.variable.field.MethodField
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class StoreVariableVisitor(
  private val mv: MethodVisitor,
  private val classScopeType: JavaType
): VariableVisitor<Unit> {
  override fun visit(variable: LocalVariable) = mv.visitVarInsn(variable.type.storeCode, variable.index)

  override fun visit(variable: BoundField) {
    TODO("Not yet implemented")
  }

  override fun visit(variable: DynamicMethodField) {
    // need to push name
    mv.visitLdcInsn(variable.name)
    // then need to swap, because the name is the first argument, then value. (for method DynamicObject.setProperty(..)
    mv.visitInsn(Opcodes.SWAP)
    visit(variable as MethodField)
  }

  override fun visit(variable: JavaClassField) = mv.visitFieldInsn(if (variable.isStatic) Opcodes.PUTSTATIC else Opcodes.PUTFIELD, variable.owner.internalName, variable.name, variable.type.descriptor)

  override fun visit(variable: MarcelArrayLengthField) {
    // field isn't settable
    throw RuntimeException("Compiler error.")
  }

  override fun visit(variable: CompositeField) {
    val field = variable.settableFieldFrom(classScopeType) ?: throw RuntimeException("Compiler error.")
    field.accept(this)
  }

  override fun visit(variable: MethodField) {
    val javaMethod = variable.setterMethod
    mv.visitMethodInsn(javaMethod.invokeCode, javaMethod.ownerClass.internalName, javaMethod.name, javaMethod.descriptor, javaMethod.ownerClass.isInterface)
  }
}