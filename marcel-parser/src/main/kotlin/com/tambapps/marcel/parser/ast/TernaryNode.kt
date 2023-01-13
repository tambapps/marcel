package com.tambapps.marcel.parser.ast

class TernaryNode(private val condition: TokenNode, private val trueExpr: TokenNode,
                  private val falseExpr: TokenNode): TokenNodeWithChild(TokenNodeType.TERNARY) {


}