package com.tambapps.marcel.semantic.scope

class MethodInnerScope(
  parentScope: MethodScope
) : MethodScope(parentScope, parentScope.typeResolver, parentScope.classType,
  parentScope.imports, parentScope.staticContext, parentScope.localVariablePool) {

}