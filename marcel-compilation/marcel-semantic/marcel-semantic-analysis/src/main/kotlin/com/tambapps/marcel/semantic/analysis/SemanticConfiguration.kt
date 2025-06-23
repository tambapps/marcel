package com.tambapps.marcel.semantic.analysis

import com.tambapps.marcel.semantic.transform.SemanticPurpose
import marcel.lang.Script

data class SemanticConfiguration(
  val scriptClass: Class<*> = Script::class.java,
  val purpose: SemanticPurpose = SemanticPurpose.COMPILATION
)