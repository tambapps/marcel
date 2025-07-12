package com.tambapps.marcel.semantic.symbol.type

enum class NullSafetyMode {
  ENABLED {
    override val isEnabled get() = true
  },
  STRICT {
    override val isEnabled get() = true
  },
  DISABLED  {
    override val isEnabled get() = false
  };

  companion object {
    val DEFAULT = DISABLED
  }
  abstract val isEnabled: Boolean
}