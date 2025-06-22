package com.tambapps.marcel.semantic.symbol.variable.field

import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.VariableVisitor

/**
 * Java field from a class
 */
abstract class JavaClassField constructor(override val type: JavaType, override val name: String, override val owner: JavaType,
                                          override val isSettable: Boolean = true): AbstractField() {

  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)


  override val isGettable = true

}
