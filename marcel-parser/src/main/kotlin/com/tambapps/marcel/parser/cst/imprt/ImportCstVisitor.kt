package com.tambapps.marcel.parser.cst.imprt

interface ImportCstVisitor<T> {

  fun visit(node: SimpleImportCstNode): T
  fun visit(node: StaticImportCstNode): T
  fun visit(node: WildcardImportCstNode): T

}