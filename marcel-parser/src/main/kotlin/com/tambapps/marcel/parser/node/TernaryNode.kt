package com.tambapps.marcel.parser.node

class TernaryNode(private val condition: TokenNode, private val trueExpr: TokenNode,
                  private val falseExpr: TokenNode): TokenNodeWithChild(TokenNodeType.TERNARY) {


}