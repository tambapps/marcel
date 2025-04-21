package com.tambapps.marcel.semantic.processor.visitor

import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver

interface ClassNodeVisitor {

  fun visit(classNode: ClassNode, symbolResolver: MarcelSymbolResolver)

}