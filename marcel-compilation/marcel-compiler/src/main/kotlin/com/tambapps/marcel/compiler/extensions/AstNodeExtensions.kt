package com.tambapps.marcel.compiler.extensions

import com.tambapps.marcel.compiler.util.ReflectUtils
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.method.MethodParameter


val ClassNode.access: Int get() = ReflectUtils.computeAccess(
  visibility, isStatic = isStatic, isEnum = isEnum, isAbstract = type.isAbstract, isInterface = type.isInterface,
  isFinal = isFinal,

  // TL;DR
  // ACC_SUPER is a class access flag used to enable modern invokespecial behavior.
  // Always include ACC_SUPER for standard Java class generation.
  // It should not be used for interfaces.
  // Omit it only when replicating very old (pre-Java 1.1) class bytecode.
  isSuper = !type.isAbstract && !type.isInterface
)

val MethodNode.access: Int get() = ReflectUtils.computeAccess(
  visibility, isStatic = isStatic, isAbstract = isAbstract, isVarArgs = isVarArgs, isSynthetic = isSynthetic
)

val FieldNode.access: Int get() = ReflectUtils.computeAccess(
  visibility, isStatic = isStatic, isFinal = isFinal, isSynthetic = isSynthetic, isEnum = isEnum
)

val MethodParameter.access: Int get() = ReflectUtils.computeAccess(
   isFinal = isFinal, isSynthetic = isSynthetic
)