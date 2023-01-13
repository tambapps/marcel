package com.tambapps.marcel.parser.type

import org.objectweb.asm.Type

class ClassType(clazz: Class<*>): JavaType {
  override val name = clazz.name
  override val internalName = Type.getInternalName(clazz)
  override val descriptor = Type.getDescriptor(clazz)
}