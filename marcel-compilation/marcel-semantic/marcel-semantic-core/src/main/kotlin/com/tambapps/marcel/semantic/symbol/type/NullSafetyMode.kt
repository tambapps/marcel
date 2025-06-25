package com.tambapps.marcel.semantic.symbol.type

enum class NullSafetyMode {
  DEFAULT {
    override val isEnabled get()= true
  },
  STRICT {
    override val isEnabled get() = true
  },
  DISABLED  {
    override val isEnabled get() = false
  };

  abstract val isEnabled: Boolean
}