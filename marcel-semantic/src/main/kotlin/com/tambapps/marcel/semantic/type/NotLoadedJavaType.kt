package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.Visibility

open class NotLoadedJavaType internal constructor(
  override val visibility: Visibility,
  override val className: String,
  override val genericTypes: List<JavaType>,
  override val superType: JavaType?,
  override val isInterface: Boolean,
  override val directlyImplementedInterfaces: MutableCollection<JavaType>): AbstractJavaType() {


  // doesn't support enum for now
  override val isEnum = false
  override val arrayType: JavaArrayType
    get() = this as NotLoadedJavaArrayType

  override val isFinal = false

  override val packageName: String?
    get() = if (className.contains('.')) className.substring(0, className.lastIndexOf(".")) else null

  override val isLoaded = false
  override val realClazz: Class<*>
    get() = throw RuntimeException("Class $className is not loaded")
  override val primitive = false
  override val realClazzOrObject = java.lang.Object::class.java
  override val isAnnotation = false

  override val allImplementedInterfaces: Collection<JavaType>
    get() {
      val allInterfaces = directlyImplementedInterfaces.flatMap {
        if (it.isLoaded) it.allImplementedInterfaces + it else listOf(it)
      }.toMutableSet()
      if (superType != null) allInterfaces.addAll(superType!!.allImplementedInterfaces)
      return allInterfaces
    }

  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    throw UnsupportedOperationException("Doesn't support generics for marcel classes")
  }

  open fun addImplementedInterface(javaType: JavaType) {
    directlyImplementedInterfaces.add(javaType)
  }
}
