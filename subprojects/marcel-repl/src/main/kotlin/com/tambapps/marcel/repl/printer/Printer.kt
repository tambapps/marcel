package com.tambapps.marcel.repl.printer

/**
 * Interface representing an object printing data
 */
interface Printer {
  suspend fun print(s: CharSequence?)
  suspend fun print(o: Any?) {
    print(if (o is CharSequence) o else o.toString())
  }

  suspend fun println(s: CharSequence?)
  suspend fun println(o: Any?) {
    println(if (o is CharSequence) o else o.toString())
  }

  suspend fun println()
}