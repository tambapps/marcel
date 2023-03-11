package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken

class ModuleNode constructor(
  val classes: MutableList<ClassNode>,
  val imports: List<ImportNode>,
  val dumbbells: Set<String>
  ) : AstNode {

  override val token = LexToken.dummy()

  constructor(imports: List<ImportNode>, dumbbells: Set<String>): this(mutableListOf(), imports, dumbbells)
  constructor(): this(emptyList(),  emptySet())
  override fun toString(): String {
    return if (classes.size == 1) {
      classes[0].toString()
    } else {
      "module (\n" + classes.joinToString(separator = "\n") + "\n)"
    }
  }

}