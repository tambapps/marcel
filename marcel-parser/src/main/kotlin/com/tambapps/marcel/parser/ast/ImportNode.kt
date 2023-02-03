package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.ReflectJavaMethod

interface ImportNode: AstNode {
  fun resolveClassName(classSimpleName: String): String?
  fun resolveMethod(methodName: String, argumentTypes: List<AstTypedObject>): JavaMethod? {
    return null
  }

}
class SimpleImportNode(private val value: String, private val asName: String? = null): ImportNode {
  override fun resolveClassName(classSimpleName: String): String? {
    return if (asName != null) {
      if (classSimpleName == asName) value
      else null
    } else {
      if (classSimpleName == value.substring(value.lastIndexOf('.') + 1)) value
      else null
    }
  }

  override fun toString(): String {
    return if (asName != null) "import $value as $asName" else "import $value"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as SimpleImportNode

    if (value != other.value) return false
    if (asName != other.asName) return false

    return true
  }

  override fun hashCode(): Int {
    var result = value.hashCode()
    result = 31 * result + (asName?.hashCode() ?: 0)
    return result
  }
}

class StaticImportNode(private val className: String, private val methodName: String): ImportNode {
  override fun resolveClassName(classSimpleName: String): String? {
    return null
  }

  override fun resolveMethod(methodName: String, argumentTypes: List<AstTypedObject>): JavaMethod? {
    if (methodName != this.methodName) return null
    val candidates = Class.forName(className).declaredMethods.filter { it.name == methodName }
      .map { ReflectJavaMethod(it) }
    return candidates.find { it.matches(methodName, argumentTypes) }
  }

  override fun toString(): String {
    return "import static $className.$methodName"
  }
}

class WildcardImportNode(private val prefix: String): ImportNode {

  override fun resolveClassName(classSimpleName: String): String? {
    return try {
      Class.forName("$prefix.$classSimpleName").name
    } catch (e: ClassNotFoundException) {
       null
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as WildcardImportNode

    if (prefix != other.prefix) return false

    return true
  }

  override fun hashCode(): Int {
    return prefix.hashCode()
  }

  override fun toString(): String {
    return "import $prefix.*"
  }
}