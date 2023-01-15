package com.tambapps.marcel.parser.type

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class JavaClassType(clazz: Class<*>): JavaType {
  override val className: String = clazz.name
  private val simpleName: String = clazz.simpleName
  override val internalName: String = Type.getInternalName(clazz)
  override val descriptor: String = Type.getDescriptor(clazz)
  override val storeCode = Opcodes.ASTORE
  override val loadCode = Opcodes.ALOAD
  override val returnCode = Opcodes.ARETURN

  override fun toString(): String {
    return simpleName
  }
}