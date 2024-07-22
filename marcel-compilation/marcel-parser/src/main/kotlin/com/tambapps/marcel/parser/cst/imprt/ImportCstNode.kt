package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.visitor.ImportCstVisitor

/**
 * CST node for an import instruction
 */
interface ImportCstNode: CstNode {

  fun <T> accept(visitor: ImportCstVisitor<T>): T
}