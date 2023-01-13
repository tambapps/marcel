package com.tambapps.marcel.parser.ast

class TokenNode(val type: TokenNodeType, val _value: String?) {
  private var _children: MutableList<TokenNode>? = null
  val children: MutableList<TokenNode>
    get() {
      return if (_children == null) {
        val list = mutableListOf<TokenNode>()
        _children = list
        list
      } else {
        _children!!
      }
  }
  val value: String
    get() = _value!!
  constructor(type: TokenNodeType, children: MutableList<TokenNode>): this(type, null, children) {

  }
  constructor(type: TokenNodeType, value: String?, children: MutableList<TokenNode>): this(type, value) {
    this._children = children
  }

  fun addChild(node: TokenNode) {
    children.add(node)
  }
}