package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.field.MarcelField

/**
 * The scope of a given node, holding data about variables and methods it can access
 */
interface Scope {

  val classType: JavaType
  val forExtensionType: JavaType?

  // TODO this method needs to check visibility (access) of the resolved type
  fun resolveTypeOrThrow(node: TypeCstNode): JavaType

  fun findField(name: String): MarcelField?

  fun hasLocalVariable(name: String): Boolean {
    return findLocalVariable(name) != null
  }

  fun findLocalVariable(name: String): LocalVariable?

  fun dispose() {}
}