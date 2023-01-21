package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.scope.Scope

interface ScopedNode<T: Scope> {

  val scope: T
}