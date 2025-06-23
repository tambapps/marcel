package com.tambapps.marcel.semantic.symbol

import com.tambapps.marcel.semantic.symbol.type.JavaTyped

interface Symbol: JavaTyped, NullAware {
  val name: String
  val isFinal: Boolean
}