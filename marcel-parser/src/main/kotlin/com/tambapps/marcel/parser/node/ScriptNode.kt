package com.tambapps.marcel.parser.node

class ScriptNode(val className: String,
                 children: MutableList<TokenNode>) : TokenNodeWithChild(TokenNodeType.SCRIPT, children) {
}