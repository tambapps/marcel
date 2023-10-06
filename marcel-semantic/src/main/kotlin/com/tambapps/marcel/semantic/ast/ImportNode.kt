package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.JavaTyped

interface ImportNode: Ast2Node {
  fun resolveClassName(typeResolver: JavaTypeResolver, classSimpleName: String): String?
  fun resolveMethod(typeResolver: JavaTypeResolver, methodName: String, argumentTypes: List<JavaTyped>): JavaMethod? {
    return null
  }

}
class SimpleImportNode(private val value: String, private val asName: String? = null,
                       override val tokenStart: LexToken, override val tokenEnd: LexToken): ImportNode {
  override fun resolveClassName(typeResolver: JavaTypeResolver, classSimpleName: String): String? {
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

class StaticImportNode(private val className: String, private val methodName: String,
                       override val tokenStart: LexToken, override val tokenEnd: LexToken): ImportNode {


  override fun resolveClassName(typeResolver: JavaTypeResolver, classSimpleName: String): String? {
    return null
  }

  override fun resolveMethod(typeResolver: JavaTypeResolver, methodName: String, argumentTypes: List<JavaTyped>): JavaMethod? {
    if (methodName != this.methodName) return null
    val type = typeResolver.of(className, emptyList())
    typeResolver.getDeclaredMethods(type)
    val candidates = typeResolver.getDeclaredMethods(type).filter { it.name == methodName }
    return typeResolver.findMatchingMethod(candidates, methodName, argumentTypes)
  }

  override fun toString(): String {
    return "import static $className.$methodName"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is StaticImportNode) return false

    if (className != other.className) return false
    if (methodName != other.methodName) return false

    return true
  }

  override fun hashCode(): Int {
    var result = className.hashCode()
    result = 31 * result + methodName.hashCode()
    return result
  }
}

class WildcardImportNode(private val prefix: String,
                         override val tokenStart: LexToken, override val tokenEnd: LexToken): ImportNode {


  override fun resolveClassName(typeResolver: JavaTypeResolver, classSimpleName: String): String? {
    return try {
      typeResolver.of("$prefix.$classSimpleName", emptyList()).className
    } catch (e: MarcelSemanticException) {
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