package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.AbstractCstNode
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.CstNodeVisitor
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor

abstract class AbstractImportCstNode(parent: CstNode?, tokenStart: LexToken, tokenEnd: LexToken) :
  AbstractCstNode(parent, tokenStart, tokenEnd), CstImportNode {

    constructor(parent: CstNode?, token: LexToken): this(parent, token, token)

  }