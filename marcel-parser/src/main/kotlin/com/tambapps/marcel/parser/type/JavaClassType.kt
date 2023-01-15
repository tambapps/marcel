package com.tambapps.marcel.parser.type

import org.objectweb.asm.Type

class JavaClassType(clazz: Class<*>): JavaType {
  override val className = clazz.name
  val simpleName = clazz.simpleName
  override val internalName = Type.getInternalName(clazz)
  override val descriptor = Type.getDescriptor(clazz)
  override val storeCode: Int
    get() = TODO("Not yet implemented")
  override val loadCode: Int
    get() = TODO("Not yet implemented")
  override val returnCode: Int
    get() = TODO("Not yet implemented")

  override fun toString(): String {
    return simpleName
  }
}