package com.tambapps.marcel.parser.ast

class ScriptNode(val className: String,
                 children: MutableList<Statement>) : TokenNodeWithChild<Statement>(TokenNodeType.SCRIPT, children) {
}