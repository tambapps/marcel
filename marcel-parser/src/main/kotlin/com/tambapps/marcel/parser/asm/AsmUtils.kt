package com.tambapps.marcel.parser.asm

import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.AstTypedObject
import com.tambapps.marcel.parser.type.JavaType
import org.objectweb.asm.Type
import java.lang.reflect.Method

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

  fun getDescriptor(method: Method): String {
    val builder = StringBuilder().append('(')
    method.parameterTypes.joinTo(builder, separator = "", transform = { getClassDescriptor(it) })
    return builder.append(')')
      .append(getClassDescriptor(method.returnType))
      .toString()
  }
  fun getDescriptor(method: MethodNode): String {
    return getDescriptor(method.parameters, method.returnType)
  }
  fun getDescriptor(parameters: List<AstTypedObject>, returnType: JavaType): String {
    val builder = StringBuilder().append('(')
    parameters.joinTo(builder, separator = "", transform = { it.type.descriptor })
    return builder.append(')')
      .append(returnType.descriptor)
      .toString()
  }
}