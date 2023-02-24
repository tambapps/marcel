package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.lexer.LexToken

class ModuleNode(val classes: MutableList<ClassNode>): AstNode {

  override val token = LexToken.dummy()

  constructor(): this(mutableListOf())
  override fun toString(): String {
    return if (classes.size == 1) {
      classes[0].toString()
    } else {
      "module (\n" + classes.joinToString(separator = "\n") + "\n)"
    }
  }

}