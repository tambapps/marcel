package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.extensions.javaType
import com.tambapps.marcel.semantic.type.JavaType
import marcel.lang.MarcelClassLoader
import marcel.lang.methods.DefaultMarcelMethods

open class JavaTypeResolver constructor(private val classLoader: MarcelClassLoader?) {

  constructor(): this(null)

  private val _definedTypes = mutableMapOf<String, JavaType>()
  val definedTypes get() = _definedTypes.values.toList()

  // extension methods or methods of marcel source code we're compiling
  //private val marcelMethods = mutableMapOf<String, MutableList<JavaMethod>>()
  //private val fieldResolver = FieldResolver()



}