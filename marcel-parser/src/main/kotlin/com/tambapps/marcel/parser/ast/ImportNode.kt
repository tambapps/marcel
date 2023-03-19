package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.type.JavaMethod

interface ImportNode: AstNode {
  fun resolveClassName(typeResolver: AstNodeTypeResolver, classSimpleName: String): String?
  fun resolveMethod(typeResolver: AstNodeTypeResolver, methodName: String, argumentTypes: List<AstTypedObject>): JavaMethod? {
    return null
  }

}
class SimpleImportNode(override val token: LexToken, private val value: String, private val asName: String? = null): ImportNode {
  override fun resolveClassName(typeResolver: AstNodeTypeResolver, classSimpleName: String): String? {
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

class StaticImportNode(override val token: LexToken, private val className: String, private val methodName: String): ImportNode {

  constructor(className: String, methodName: String): this(LexToken.dummy(), className, methodName)

  override fun resolveClassName(typeResolver: AstNodeTypeResolver, classSimpleName: String): String? {
    return null
  }

  override fun resolveMethod(typeResolver: AstNodeTypeResolver, methodName: String, argumentTypes: List<AstTypedObject>): JavaMethod? {
    if (methodName != this.methodName) return null
    val type = typeResolver.of(className, emptyList())
    typeResolver.getDeclaredMethods(type)
    val candidates = typeResolver.getDeclaredMethods(type).filter { it.name == methodName }
    return candidates.find { it.matches(typeResolver, methodName, argumentTypes) }
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

class WildcardImportNode(override val token: LexToken, private val prefix: String): ImportNode {

  constructor(prefix: String): this(LexToken.dummy(), prefix)

  override fun resolveClassName(typeResolver: AstNodeTypeResolver, classSimpleName: String): String? {
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