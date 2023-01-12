package com.tambapps.marcel.parser.node

data class FunctionCallNode(val name: String) : TokenNodeWithChild(TokenNodeType.FUNCTION_CALL) {

}