package com.tambapps.marcel.semantic.analysis

import com.tambapps.marcel.semantic.transform.SemanticPurpose
import marcel.lang.Script

open class SemanticConfiguration(
  val scriptClass: Class<*> = Script::class.java,
  val purpose: SemanticPurpose = SemanticPurpose.COMPILATION
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is SemanticConfiguration) return false

    if (scriptClass != other.scriptClass) return false
    if (purpose != other.purpose) return false

    return true
  }

  override fun hashCode(): Int {
    var result = scriptClass.hashCode()
    result = 31 * result + purpose.hashCode()
    return result
  }
}