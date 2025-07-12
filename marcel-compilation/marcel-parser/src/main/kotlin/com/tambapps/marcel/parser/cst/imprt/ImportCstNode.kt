package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.parser.cst.IdentifiableCstNode
import com.tambapps.marcel.parser.cst.visitor.ImportCstNodeVisitor

/**
 * CST node for an import instruction
 */
interface ImportCstNode: IdentifiableCstNode {

  fun <T> accept(visitor: ImportCstNodeVisitor<T>): T
}