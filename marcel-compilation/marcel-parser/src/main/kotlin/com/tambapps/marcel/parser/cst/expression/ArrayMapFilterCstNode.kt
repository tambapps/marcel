package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.TypeCstNode

// TODO document me.
//   list<int> list2 = [for int a in list -> a + 1 if a <= 2]
//    list<int> list2 = [for int a in list if a <= 2]
//    int list2 = for int a in list if a <= 2 NYI // find


// TODO test me
class ArrayMapFilterCstNode(
  parent: CstNode?,
  tokenStart: LexToken,
  tokenEnd: LexToken,
  varType: TypeCstNode,
  varName: String,
  inExpr: ExpressionCstNode,
  val mapExpr: ExpressionCstNode?,
  filterExpr: ExpressionCstNode?,
) : InOperationCstNode(parent, tokenStart, tokenEnd, varType, varName, inExpr, filterExpr) {

  override fun <T, U> accept(visitor: ExpressionCstNodeVisitor<T, U>, arg: U?) = visitor.visit(this, arg)

}