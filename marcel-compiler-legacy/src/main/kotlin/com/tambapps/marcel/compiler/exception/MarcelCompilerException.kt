package com.tambapps.marcel.compiler.exception

class MarcelCompilerException(message: String?, cause: Throwable?) : RuntimeException(message, cause) {
  constructor(message: String?): this(message, null)
  constructor(throwable: Throwable): this(null, throwable)
}