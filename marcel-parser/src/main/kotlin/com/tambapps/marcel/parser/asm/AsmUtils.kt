package com.tambapps.marcel.parser.asm

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.type.JavaType

object AsmUtils {

  fun getInternalName(clazz: Class<*>): String {
    return getInternalName(clazz.name)
  }

  fun getInternalName(className: String): String {
    return className.replace('.', '/')
  }

  fun getDescriptor(parameters: List<JavaType>, returnType: JavaType): String {
    val builder = StringBuilder().append('(')
    parameters.joinTo(builder, separator = "", transform = { it.descriptor })
    return builder.append(')')
      .append(returnType.descriptor)
      .toString()
  }
}