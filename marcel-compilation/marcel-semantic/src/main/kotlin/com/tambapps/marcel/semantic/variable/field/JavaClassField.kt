package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.VariableVisitor

/**
 * Java field from a class
 */
abstract class JavaClassField constructor(override val type: JavaType, override val name: String, override val owner: JavaType,
                                          override val isSettable: Boolean = true): AbstractField() {

  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)


  override val isGettable = true

}
