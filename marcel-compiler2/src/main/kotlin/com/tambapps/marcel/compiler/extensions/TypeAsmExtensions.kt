package com.tambapps.marcel.compiler.extensions

import com.tambapps.marcel.compiler.util.AsmUtils
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped

val JavaTyped.internalName: String get() = AsmUtils.getInternalName(type)
val JavaTyped.descriptor: String get() {
  val type = this.type
  return if (type.isLoaded) AsmUtils.getClassDescriptor(type.realClazz)
  else {
    val descriptor = AsmUtils.getObjectClassDescriptor(type.className)
    if (type.isAnnotation) "@$descriptor" else descriptor
  }
}

val JavaType.signature: String get() {
  if (primitive) return descriptor
  val builder = StringBuilder("L$internalName")
  if (genericTypes.isNotEmpty()) {
    genericTypes.joinTo(buffer = builder, separator = "", prefix = "<", postfix = ">", transform = { it.descriptor })
  }
  builder.append(";")
  directlyImplementedInterfaces.joinTo(buffer = builder, separator = "", transform = { it.signature })
  return builder.toString()
}
