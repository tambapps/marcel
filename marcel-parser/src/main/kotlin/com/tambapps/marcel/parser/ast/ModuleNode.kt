package com.tambapps.marcel.parser.ast

class ModuleNode(val classes: MutableList<ClassNode>): AstNode {
  override fun toString(): String {
    return if (classes.size == 1) {
      classes[0].toString()
    } else {
      "module (\n" + classes.joinToString(separator = "\n") + "\n)"
    }
  }
}