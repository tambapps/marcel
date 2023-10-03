package com.tambapps.marcel.parser.cst

interface CstNodeVisitor<T> {

    fun visit(node: AbstractCstNode): T

}