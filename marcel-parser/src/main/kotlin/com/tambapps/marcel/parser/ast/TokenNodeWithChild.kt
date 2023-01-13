package com.tambapps.marcel.parser.ast

open class TokenNodeWithChild<T : TokenNode>(override val type: TokenNodeType, val children: MutableList<T>): TokenNode {

  constructor(type: TokenNodeType): this(type, mutableListOf())

  fun addChildren(vararg nodes: T) {
    children.addAll(nodes)
  }

  fun addChild(node: T) {
    children.add(node)
  }
}