package com.tambapps.marcel.semantic.type

import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType

/**
 * [JavaType] of a class that is loaded on the classpath and therefore available in Java reflect APIs
 */
abstract class LoadedJavaType internal constructor(final override val realClazz: Class<*>, final override val genericTypes: List<JavaType>): AbstractJavaType() {
  override val isLoaded = true
  override val isEnum = realClazz.isEnum
  override val isScript = false
  override val isArray = realClazz.isArray

  override val className: String = realClazz.name
  override val isFinal = (realClazz.modifiers and Modifier.FINAL) != 0
  override val superType get() =  if (realClazz.superclass != null) JavaType.of(realClazz.superclass) else null

  override val isAnnotation = realClazz.isAnnotation
  override val asPrimitiveType: JavaPrimitiveType
    get() = when(realClazz) {
      JavaType.Integer.realClazz -> JavaType.int
      JavaType.Long.realClazz -> JavaType.long
      JavaType.Character.realClazz -> JavaType.char
      JavaType.Float.realClazz -> JavaType.float
      JavaType.Double.realClazz -> JavaType.double
      JavaType.Boolean.realClazz -> JavaType.boolean
      JavaType.Byte.realClazz -> JavaType.byte
      JavaType.Short.realClazz -> JavaType.short
      else -> super.asPrimitiveType
    }

  override val isInterface = realClazz.isInterface
  private var _interfaces: Set<JavaType>? = null
  override val allImplementedInterfaces: Collection<JavaType>
    get() {
      if (_interfaces == null) {
        _interfaces = getAllImplementedInterfacesRecursively(realClazz).asSequence()
          .toSet()
      }
      return _interfaces!!
    }

  override fun getAnnotation(javaAnnotationType: JavaAnnotationType): JavaAnnotation? {
    throw UnsupportedOperationException()
  }

  override val directlyImplementedInterfaces: Collection<JavaType>
    get() = realClazz.interfaces.map { toJavaType(realClazz, it) }

  private fun toJavaType(realClazz: Class<*>, interfaze: Class<*>): JavaType {
    val genericInterface = realClazz.genericInterfaces
        .mapNotNull { it as? ParameterizedType }
        .find { it.rawType.typeName == interfaze.typeName }
    var type = JavaType.of(interfaze)
    if (genericInterface != null) {
      val thisClassGenericTypes = realClazz.typeParameters
      val interfaceTypeParameters = genericInterface.actualTypeArguments
      val genericTypes = interfaceTypeParameters.map { intTypeParam ->
        val i = thisClassGenericTypes.indexOfFirst { it.name == intTypeParam.typeName }
        return@map if (i >= 0 && i < genericTypes.size) genericTypes[i] else JavaType.Object
      }
      type = type.withGenericTypes(genericTypes)
    }
    return type
  }

  private fun getAllImplementedInterfacesRecursively(c: Class<*>): Set<JavaType> {
    var clazz = c
    val res = mutableSetOf<JavaType>()
    do {
      // First, add all the interfaces implemented by this class
      val interfaces = clazz.interfaces.map {
        toJavaType(clazz, it)
      }
      res.addAll(interfaces)
      for (interfaze in interfaces) {
        res.addAll(getAllImplementedInterfacesRecursively(interfaze.realClazz))
      }
      // Add the super class
      val superClass = clazz.superclass ?: break
      // Interfaces does not have java,lang.Object as superclass, they have null, so break the cycle and return
      // Now inspect the superclass
      clazz = superClass
    } while (JavaType.Object.realClazz != clazz)
    return res
  }

  override val primitive = realClazz.isPrimitive
  override val realClazzOrObject = realClazz

  override fun hashCode(): Int {
    var result = className.hashCode()
    result = 31 * result + genericTypes.hashCode()
    return result
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is JavaType) return false

    if (className != other.className) return false
    if (genericTypes != other.genericTypes) return false

    return true
  }

}
