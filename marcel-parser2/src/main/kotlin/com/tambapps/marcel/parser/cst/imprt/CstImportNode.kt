package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.parser.cst.CstNode

interface CstImportNode: CstNode {

  fun <T> accept(visitor: CstImportVisitor<T>): T
}