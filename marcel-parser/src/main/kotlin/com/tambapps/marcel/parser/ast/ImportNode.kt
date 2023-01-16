package com.tambapps.marcel.parser.ast
interface ImportNode: AstNode {
  fun resolve(classSimpleName: String): String?

  }
class SimpleImportNode(private val value: String, private val asName: String? = null): ImportNode {
  override fun resolve(classSimpleName: String): String? {
    return if (asName != null) {
      if (classSimpleName == asName) classSimpleName
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

class WildcardImportNode(private val prefix: String): ImportNode {

  override fun resolve(classSimpleName: String): String? {
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