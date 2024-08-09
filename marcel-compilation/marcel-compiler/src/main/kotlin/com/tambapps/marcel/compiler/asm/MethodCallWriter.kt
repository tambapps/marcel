package com.tambapps.marcel.compiler.asm

import com.tambapps.marcel.compiler.extensions.descriptor
import com.tambapps.marcel.compiler.extensions.internalName
import com.tambapps.marcel.compiler.extensions.invokeCode
import com.tambapps.marcel.semantic.method.CastMethod
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.JavaMethodVisitor
import org.objectweb.asm.MethodVisitor

class MethodCallWriter(
  private val methodVisitor: MethodVisitor
): JavaMethodVisitor<Unit> {
  override fun visit(method: JavaMethod) {
    methodVisitor.visitMethodInsn(method.invokeCode, method.ownerClass.internalName, method.name, method.descriptor, method.ownerClass.isInterface)
  }

  override fun visit(method: CastMethod) {
    // nothing to do. this method is intended to be used with the castType, and the methodExpressionWriter already handles it
  }
}