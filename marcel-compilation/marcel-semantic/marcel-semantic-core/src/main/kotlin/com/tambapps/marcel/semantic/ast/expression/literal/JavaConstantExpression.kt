package com.tambapps.marcel.semantic.ast.expression.literal

import com.tambapps.marcel.semantic.symbol.NullAware
import com.tambapps.marcel.semantic.symbol.type.Nullness

/**
 * A Java constant that can be used for annotation attributes
 */
interface JavaConstantExpression: NullAware {
  val value: Any?
  override val nullness: Nullness
    get() = Nullness.NOT_NULL
}