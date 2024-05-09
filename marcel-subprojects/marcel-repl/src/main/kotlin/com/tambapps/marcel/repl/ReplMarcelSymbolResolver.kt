package com.tambapps.marcel.repl

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.variable.field.BoundField
import com.tambapps.marcel.semantic.variable.field.CompositeField
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader

class ReplMarcelSymbolResolver constructor(
  classLoader: MarcelClassLoader?
) : MarcelSymbolResolver(classLoader) {

  private val _libraryClasses = mutableListOf<ClassNode>()
  private val _scriptVariables = mutableMapOf<String, BoundField>()

  val libraryClasses: List<ClassNode> get() = _libraryClasses
  val scriptVariables: Map<String, BoundField> get() = _scriptVariables

  fun defineLibraryClass(classNode: ClassNode) {
    defineType(classNode)
    _libraryClasses.add(classNode)
  }

  fun defineBoundField(field: BoundField) {
    defineField(field.owner, field)
    _scriptVariables[field.name] = field
  }

  fun getBoundField(name: String): BoundField? = _scriptVariables[name]

  override fun findField(javaType: JavaType, name: String): CompositeField? {
    val f = super.findField(javaType, name)
    if (f != null || !javaType.isScript) return f
    // if we're looking for a variable of a script, it may be a BoundField
    return _scriptVariables[name]?.withOwner(javaType)?.let { CompositeField(it) }
  }
}