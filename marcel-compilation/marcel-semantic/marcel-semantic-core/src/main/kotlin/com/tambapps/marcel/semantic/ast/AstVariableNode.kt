package com.tambapps.marcel.semantic.ast

import com.tambapps.marcel.semantic.symbol.variable.Variable

interface AstVariableNode : AstNode {
  var variable: Variable
}