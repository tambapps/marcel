package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.ReflectJavaMethod
import marcel.lang.MarcelClassLoader

open class JavaTypeResolver constructor(private val classLoader: MarcelClassLoader?) {

  constructor(): this(null)

  private val _definedTypes = mutableMapOf<String, JavaType>()
  val definedTypes get() = _definedTypes.values.toList()

  // extension methods or methods of marcel source code we're compiling
  private val marcelMethods = mutableMapOf<String, MutableList<JavaMethod>>()
  //private val fieldResolver = FieldResolver()


  fun getDeclaredMethods(javaType: JavaType): List<JavaMethod> {
    return if (javaType.isLoaded) javaType.realClazz.declaredMethods.map { ReflectJavaMethod(it, javaType) }
    else marcelMethods[javaType.className] ?: emptyList()
  }

  fun getInterfaceLambdaMethod(type: JavaType): JavaMethod {
    return getDeclaredMethods(type).first { it.isAbstract }
  }

  private fun loadAllMethods(javaType: JavaType, excludeInterfaces: Boolean = false): Set<JavaMethod> {
    val methods = mutableSetOf<JavaMethod>()
    if (javaType.isLoaded) {
      javaType.realClazz.declaredMethods.forEach { methods.add(ReflectJavaMethod(it)) }
    }
    marcelMethods[javaType.className]?.let { methods.addAll(it) }

    var type = javaType.superType
    while (type != null) {
      methods.addAll(loadAllMethods(type, true))
      type = type.superType
    }
    if (!excludeInterfaces) {
      methods.addAll(javaType.allImplementedInterfaces.flatMap { loadAllMethods(it, true) })
    }
    return methods
  }

}