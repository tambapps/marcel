package com.tambapps.marcel.parser.type

import org.objectweb.asm.Type

class ClassType(clazz: Class<*>): JavaType {
  override val className = clazz.name
  override val internalName = Type.getInternalName(clazz)
  override val descriptor = Type.getDescriptor(clazz)
  override val storeCode: Int
    get() = TODO("Not yet implemented")
  override val loadCode: Int
    get() = TODO("Not yet implemented")
}