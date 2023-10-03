package com.tambapps.marcel.parser.cst

interface CstNodeVisitor<T> {

    fun visit(node: CstNode): T

}