package com.tambapps.marcel.compiler.extensions

import com.tambapps.marcel.compiler.util.ReflectUtils
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.FieldNode
import com.tambapps.marcel.semantic.ast.MethodNode


val ClassNode.access: Int get() = ReflectUtils.computeAccess(
  visibility, isStatic = isStatic
)

val MethodNode.access: Int get() = ReflectUtils.computeAccess(
  visibility, isStatic = isStatic, isAbstract = isAbstract,
)

val FieldNode.access: Int get() = ReflectUtils.computeAccess(
  visibility, isStatic = isStatic, isFinal = isFinal, isSynthetic = isSynthetic
)