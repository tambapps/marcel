package com.tambapps.marcel.semantic.symbol

import com.tambapps.marcel.semantic.symbol.type.Nullness

interface NullAware {
  val nullness: Nullness
}