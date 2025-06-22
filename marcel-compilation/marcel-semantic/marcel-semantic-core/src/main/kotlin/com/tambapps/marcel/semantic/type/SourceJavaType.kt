package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.annotation.JavaAnnotation

/**
 * A [JavaType] that is NOT loaded on the classpath. Such classes always represents an Object as Java
 * doesn't allow to define new primitives
 */
open class SourceJavaType constructor(
  override val visibility: Visibility,
  override val className: String,
  override val genericTypes: List<JavaType>,
  override var superType: JavaType?,
  override val isInterface: Boolean,
  override val isFunctionalInterface: Boolean,
  override val directlyImplementedInterfaces: MutableCollection<JavaType>,
  override val isScript: Boolean,
  override val isEnum: Boolean,
  override val isAbstract: Boolean,
  override val isFinal: Boolean,
  override val isAnnotation: Boolean,
  override val isExtensionType: Boolean,
  override var globalExtendedType: JavaType?,
  override val nullness: Nullness = Nullness.NOT_NULL
): AbstractJavaType() {

  private val annotations = mutableListOf<JavaAnnotation>()

  override fun getAnnotation(javaAnnotationType: JavaAnnotationType): JavaAnnotation? {
    return annotations.find { it.type == javaAnnotationType }
  }

  override val isArray = false
  override val primitive = false
  override val isLoaded = false

  override val packageName: String?
    get() = if (className.contains('.')) className.substring(0, className.lastIndexOf(".")) else null

  override val realClazz: Class<*>
    get() = throw RuntimeException("Class $className is not loaded")

  override val allImplementedInterfaces: Collection<JavaType>
    get() {
      val allInterfaces = directlyImplementedInterfaces.flatMap {
        if (it.isLoaded) it.allImplementedInterfaces + it else listOf(it)
      }.toMutableSet()
      if (superType != null) allInterfaces.addAll(superType!!.allImplementedInterfaces)
      return allInterfaces
    }

  override val arrayType: JavaArrayType
    get() = SourceJavaArrayType(this)
  override fun withGenericTypes(genericTypes: List<JavaType>): JavaType {
    if (genericTypes.isNotEmpty()) {
      throw UnsupportedOperationException("Doesn't support generics for marcel classes")
    }
    return this
  }

  open fun addImplementedInterface(javaType: JavaType) {
    directlyImplementedInterfaces.add(javaType)
  }

  open fun addAnnotation(annotation: JavaAnnotation) {
    annotations.add(annotation)
  }

}
