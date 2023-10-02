package com.tambapps.marcel.parser.cst.expression.reference

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode

class IndexAccessCstNode(parent: CstNode?,
                         val ownerNode: CstNode, // the owner of the access, the 'a' in a[...]
                         val indexNodes: List<CstNode>,
                         val isSafeAccess: Boolean,
                         tokenStart: LexToken, tokenEnd: LexToken) :
    CstNode(parent, tokenStart, tokenEnd) {

}