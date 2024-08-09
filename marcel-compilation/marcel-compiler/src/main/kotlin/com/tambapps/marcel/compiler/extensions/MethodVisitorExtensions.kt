package com.tambapps.marcel.compiler.extensions

import com.tambapps.marcel.semantic.method.MarcelMethod
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

fun MethodVisitor.visitSuperMethodInsn(javaMethod: MarcelMethod) {
  visitMethodInsn(Opcodes.INVOKESPECIAL, javaMethod.ownerClass.internalName, javaMethod.name, javaMethod.descriptor, javaMethod.ownerClass.isInterface)
}