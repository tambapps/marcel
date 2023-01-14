package com.tambapps.marcel.parser.visitor

import com.tambapps.marcel.parser.ast.DropNode

interface StatementVisitor {

  fun visit(dropNode: DropNode)

}