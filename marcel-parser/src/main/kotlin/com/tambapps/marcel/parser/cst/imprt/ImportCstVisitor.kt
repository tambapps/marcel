package com.tambapps.marcel.parser.cst.imprt

interface ImportCstVisitor<T> {

  fun visit(node: SimpleImportNode): T
  fun visit(node: StaticImportNode): T
  fun visit(node: WildcardImportNode): T

}