package com.tambapps.marcel.repl

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.variable.field.BoundField
import com.tambapps.marcel.semantic.variable.field.CompositeField
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader

class ReplJavaTypeResolver constructor(classLoader: MarcelClassLoader?, private val binding: Binding) : JavaTypeResolver(classLoader) {

  private val _libraryClasses = mutableListOf<ClassNode>()
  private val scriptVariables = mutableMapOf<String, BoundField>()

  val libraryClasses: List<ClassNode> get() = _libraryClasses

  fun defineLibraryClass(classNode: ClassNode) {
    defineClass(classNode)
    _libraryClasses.add(classNode)
  }

  fun defineBoundField(field: BoundField) {
    defineField(field.owner, field)
    scriptVariables[field.name] = field
  }

  fun getBoundField(name: String): BoundField? = scriptVariables[name]

  override fun findField(javaType: JavaType, name: String): CompositeField? {
    val f = super.findField(javaType, name)
    if (f != null || !javaType.isScript) return f
    // if we're looking for a variable of a script, it may be a BoundField
    return scriptVariables[name]?.withOwner(javaType)?.let { CompositeField(it) }
  }
}