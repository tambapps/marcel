package com.tambapps.marcel.repl.printer

interface Printer {

  fun print(s: String)
  fun println(s: String)
  fun println(o: Any?)
  fun println()

}