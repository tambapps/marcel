package com.tambapps.marcel.parser.cst.visitor

import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.EnumCstNode
import com.tambapps.marcel.parser.cst.RegularClassCstNode
import com.tambapps.marcel.parser.cst.ScriptCstNode

interface ClassCstNodeVisitor<T> {

  fun visit(node: RegularClassCstNode): T
  fun visit(node: EnumCstNode): T
  fun visit(node: ScriptCstNode): T

}