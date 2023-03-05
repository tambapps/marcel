package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken

class ModuleNode constructor(
  val dumbbells: Set<String>,
  val classes: MutableList<ClassNode>
) : AstNode {

  override val token = LexToken.dummy()

  constructor(dumbbells: Set<String>): this(dumbbells,  mutableListOf())
  constructor(): this(emptySet())
  override fun toString(): String {
    return if (classes.size == 1) {
      classes[0].toString()
    } else {
      "module (\n" + classes.joinToString(separator = "\n") + "\n)"
    }
  }

}