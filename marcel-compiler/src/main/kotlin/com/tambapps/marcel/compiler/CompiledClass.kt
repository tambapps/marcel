package com.tambapps.marcel.compiler

data class CompiledClass constructor(val className: String, val bytes: ByteArray) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CompiledClass) return false

    if (className != other.className) return false
    if (!bytes.contentEquals(other.bytes)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = className.hashCode()
    result = 31 * result + bytes.contentHashCode()
    return result
  }
}