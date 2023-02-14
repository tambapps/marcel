package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.ast.visitor.CheckAllPathsReturnVisitor

interface AstInstructionNode: AstNode {

  fun <T> accept(astNodeVisitor: AstNodeVisitor<T>): T
  fun allBranchesReturn(): Boolean = accept(CheckAllPathsReturnVisitor())

}