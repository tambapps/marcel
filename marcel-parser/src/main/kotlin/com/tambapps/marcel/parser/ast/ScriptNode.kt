package com.tambapps.marcel.parser.ast

class ScriptNode(val className: String,
                 children: MutableList<TokenNode>) : TokenNodeWithChild(TokenNodeType.SCRIPT, children) {
}