package com.tambapps.marcel.dumbbell

class DumbbellException : RuntimeException {
  constructor(message: String) : super(message)

  constructor(message: String, cause: Throwable) : super(message, cause)

  constructor(cause: Throwable) : super(cause.message, cause)
}
