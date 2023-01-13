package com.tambapps.marcel.parser.ast

open class TokenNodeWithChild(type: TokenNodeType, val children: MutableList<TokenNode>): TokenNode(type) {

  constructor(type: TokenNodeType): this(type, mutableListOf())

  fun addChildren(vararg nodes: TokenNode) {
    children.addAll(nodes)
  }

  fun addChild(node: TokenNode) {
    children.add(node)
  }
}