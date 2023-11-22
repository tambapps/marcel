package com.tambapps.marcel.repl

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader

class ReplJavaTypeResolver constructor(classLoader: MarcelClassLoader?, private val binding: Binding) : JavaTypeResolver(classLoader) {

  private val _libraryClasses = mutableListOf<ClassNode>()
  val libraryClasses: List<ClassNode> get() = _libraryClasses

  fun defineLibraryClass(classNode: ClassNode) {
    defineClass(classNode)
    _libraryClasses.add(classNode)
  }

}