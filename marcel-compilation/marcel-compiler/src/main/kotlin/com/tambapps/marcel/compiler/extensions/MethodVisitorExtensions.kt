package com.tambapps.marcel.compiler.extensions

import com.tambapps.marcel.semantic.method.JavaMethod
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

fun MethodVisitor.visitMethodInsn(javaMethod: JavaMethod) {
  visitMethodInsn(javaMethod.invokeCode, javaMethod.ownerClass.internalName, javaMethod.name, javaMethod.descriptor, javaMethod.ownerClass.isInterface)
}
fun MethodVisitor.visitSuperMethodInsn(javaMethod: JavaMethod) {
  visitMethodInsn(Opcodes.INVOKESPECIAL, javaMethod.ownerClass.internalName, javaMethod.name, javaMethod.descriptor, javaMethod.ownerClass.isInterface)
}