package com.tambapps.marcel.semantic.scope

/**
 * A inner scope inside a method. E.g. in a if/else, in a switch, ...
 */
class MethodInnerScope(
  parentScope: MethodScope
) : MethodScope(parentScope, parentScope.typeResolver, parentScope.classType,
  parentScope.imports, parentScope.staticContext, parentScope.localVariablePool) {

}