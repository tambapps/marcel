package com.tambapps.marcel.parser.cst.imprt

import com.tambapps.marcel.parser.cst.CstNode

interface ImportCstNode: CstNode {

  fun <T> accept(visitor: ImportCstVisitor<T>): T
}