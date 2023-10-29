package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.semantic.variable.Variable

interface AstVariableNode: Ast2Node {
  var variable: Variable
}