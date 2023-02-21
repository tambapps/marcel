package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.scope.Scope

interface ScopedNode<T: Scope> {

  var scope: T


  fun trySetScope(scope: Scope) {
    this.scope = scope as? T ?: throw RuntimeException("Compiler error. Couldn't cast scope")
  }
}