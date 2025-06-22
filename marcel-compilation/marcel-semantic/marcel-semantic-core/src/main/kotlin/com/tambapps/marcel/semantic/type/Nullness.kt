package com.tambapps.marcel.semantic.type


enum class Nullness {
  /**
   * The value can be null
   */
  NULLABLE,
  /**
   * The value can't be null
   */
  NOT_NULL,
  /**
   * The value may or may not be null
   */
  UNKNOWN,
}