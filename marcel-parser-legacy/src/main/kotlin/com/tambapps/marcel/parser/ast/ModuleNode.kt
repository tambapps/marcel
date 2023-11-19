package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.type.JavaType

class ModuleNode constructor(
  val classes: MutableList<ClassNode>,
  val imports: List<ImportNode>,
  val extensionTypes: List<JavaType>,
  val dumbbells: Set<String>
  ) : AstNode {

  override val token = LexToken.dummy()

  constructor(imports: List<ImportNode>, extensionTypes: List<JavaType>, dumbbells: Set<String>): this(mutableListOf(), imports, extensionTypes,  dumbbells)
  constructor(): this(emptyList(), emptyList(),  emptySet())
  override fun toString(): String {
    return if (classes.size == 1) {
      classes[0].toString()
    } else {
      "module (\n" + classes.joinToString(separator = "\n") + "\n)"
    }
  }

}