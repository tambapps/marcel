package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.scope.Scope

interface ResolvableNode {

  fun resolve(scope: Scope)
}