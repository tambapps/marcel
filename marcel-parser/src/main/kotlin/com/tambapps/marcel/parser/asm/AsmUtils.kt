package com.tambapps.marcel.parser.asm

object AsmUtils {

  fun getInternalName(clazz: Class<*>): String {
    return getInternalName(clazz.name)
  }

  fun getInternalName(className: String): String {
    return className.replace('.', '/')
  }

}