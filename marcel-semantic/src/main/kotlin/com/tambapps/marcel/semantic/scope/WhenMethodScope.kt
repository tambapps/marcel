package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.variable.LocalVariable

// TODO delete me
class WhenMethodScope(
  classScope: ClassScope,
  method: JavaMethod,
  private val outerScopeLocalVariable: List<LocalVariable>
) : MethodScope(classScope, method) {
}