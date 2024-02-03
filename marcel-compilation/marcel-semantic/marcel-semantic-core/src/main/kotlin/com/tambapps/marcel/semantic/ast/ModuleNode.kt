package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.lexer.LexToken


class ModuleNode constructor(tokenStart: LexToken, tokenEnd: LexToken) : AbstractAstNode(tokenStart, tokenEnd) {

  val classes = mutableListOf<ClassNode>()


  override fun toString(): String {
    return if (classes.size == 1) {
      classes[0].toString()
    } else {
      "module (\n" + classes.joinToString(separator = "\n") + "\n)"
    }
  }

}