package com.tambapps.marcel.semantic.processor.scope

import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.variable.LocalVariable
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelField

/**
 * The scope of a given node, holding data about variables and methods it can access
 */
interface Scope {

  val classType: JavaType
  val forExtensionType: JavaType?

  fun resolveTypeOrThrow(node: TypeCstNode): JavaType

  fun findField(name: String): MarcelField?

  fun hasLocalVariable(name: String): Boolean {
    return findLocalVariable(name) != null
  }

  fun findLocalVariable(name: String): LocalVariable?

  fun dispose() {}
}