package com.tambapps.marcel.semantic.visitor

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

interface ClassNodeVisitor {

  fun visit(classNode: ClassNode, symbolResolver: MarcelSymbolResolver)

}