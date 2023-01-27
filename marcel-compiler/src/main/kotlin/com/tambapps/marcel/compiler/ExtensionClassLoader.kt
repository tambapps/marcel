package com.tambapps.marcel.compiler

import com.tambapps.marcel.parser.type.ExtensionJavaMethod
import com.tambapps.marcel.parser.type.JavaType
import java.lang.reflect.Modifier

class ExtensionClassLoader {

  fun loadExtensionMethods(clazz: Class<*>) {
    clazz.declaredMethods.asSequence()
      .filter { (it.modifiers and Modifier.STATIC) != 0 && it.parameters.isNotEmpty() }
      .forEach {
        val owner = JavaType.of(it.parameters.first().type)
        owner.defineMethod(ExtensionJavaMethod(it))
      }
  }
}