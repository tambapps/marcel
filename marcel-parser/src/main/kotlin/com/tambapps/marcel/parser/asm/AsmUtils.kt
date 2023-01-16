package com.tambapps.marcel.parser.asm

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.TypedNode
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Type

object AsmUtils {

  fun getInternalName(clazz: Class<*>): String {
    return getInternalName(clazz.name)
  }

  fun getInternalName(javaType: JavaType): String {
    return getInternalName(javaType.className)
  }

  fun getInternalName(className: String): String {
    return className.replace('.', '/')
  }

  fun getClassDescriptor(clazz: Class<*>): String {
    return Type.getDescriptor(clazz)
  }
  fun getObjectClassDescriptor(className: String): String {
    return "L" + getInternalName(className) + ";"
  }
  fun getDescriptor(parameters: List<TypedNode>, returnType: JavaType): String {
    val builder = StringBuilder().append('(')
    parameters.joinTo(builder, separator = "", transform = { it.type.descriptor })
    return builder.append(')')
      .append(returnType.descriptor)
      .toString()
  }
}