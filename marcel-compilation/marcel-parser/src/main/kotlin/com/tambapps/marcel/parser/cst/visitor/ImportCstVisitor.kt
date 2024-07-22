package com.tambapps.marcel.parser.cst.visitor

import com.tambapps.marcel.parser.cst.imprt.SimpleImportCstNode
import com.tambapps.marcel.parser.cst.imprt.StaticImportCstNode
import com.tambapps.marcel.parser.cst.imprt.WildcardImportCstNode

interface ImportCstVisitor<T> {

  fun visit(node: SimpleImportCstNode): T
  fun visit(node: StaticImportCstNode): T
  fun visit(node: WildcardImportCstNode): T

}