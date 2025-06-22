package com.tambapps.marcel.repl

import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.variable.field.BoundField
import com.tambapps.marcel.semantic.symbol.variable.field.CompositeField
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelField
import marcel.lang.MarcelClassLoader

open class ReplMarcelSymbolResolver constructor(
  classLoader: MarcelClassLoader?
) : MarcelSymbolResolver(classLoader) {

  private val _scriptVariables = mutableMapOf<String, BoundField>()

  val scriptVariables: Map<String, BoundField> get() = _scriptVariables

  override fun defineField(javaType: JavaType, field: MarcelField) {
    super.defineField(javaType, field)
    if (field is BoundField) {
      _scriptVariables[field.name] = field
    }
  }

  fun getBoundField(name: String): BoundField? = _scriptVariables[name]

  override fun findField(javaType: JavaType, name: String): CompositeField? {
    val f = super.findField(javaType, name)
    if (f != null || !javaType.isScript) return f
    // if we're looking for a variable of a script, it may be a BoundField
    return _scriptVariables[name]?.withOwner(javaType)?.let { CompositeField(it) }
  }
}