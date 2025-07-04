package com.tambapps.marcel.semantic.analysis

import com.tambapps.marcel.semantic.symbol.type.NullSafetyMode
import com.tambapps.marcel.semantic.transform.SemanticPurpose
import marcel.lang.Script

data class SemanticConfiguration(
  val scriptClass: Class<*> = Script::class.java,
  val purpose: SemanticPurpose = SemanticPurpose.COMPILATION,
  // disabled by default, at least for now, because it is not completely handled
  val nullSafetyMode: NullSafetyMode = NullSafetyMode.DISABLED
)