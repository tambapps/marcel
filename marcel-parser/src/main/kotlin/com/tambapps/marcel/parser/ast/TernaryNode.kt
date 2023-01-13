package com.tambapps.marcel.parser.ast

class TernaryNode(val condition: TokenNode, val trueExpr: TokenNode,
                  val falseExpr: TokenNode): TokenNodeWithChild<TokenNode>(TokenNodeType.TERNARY) {


}