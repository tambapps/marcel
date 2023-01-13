package com.tambapps.marcel.parser.ast

data class FunctionCallNode(val name: String) : TokenNodeWithChild(TokenNodeType.FUNCTION_CALL) {

}