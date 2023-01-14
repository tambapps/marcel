package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.visitor.StatementVisitor

interface StatementNode: AstNode {

  fun accept(mv: StatementVisitor)

}