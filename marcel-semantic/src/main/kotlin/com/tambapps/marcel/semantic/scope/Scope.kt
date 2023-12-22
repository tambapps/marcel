package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeNode
import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.ast.WildcardImportNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.LocalVariable
import com.tambapps.marcel.semantic.variable.field.MarcelField

/**
 * The scope of a given node, holding data about variables and methods it can access
 */
interface Scope {

  companion object {
    val DEFAULT_IMPORTS: List<ImportNode> = listOf(
      WildcardImportNode("java.lang"),
      WildcardImportNode("java.util"),
      WildcardImportNode("java.io"),
      WildcardImportNode("marcel.lang"),
    )
  }

  val classType: JavaType
  val forExtensionType: JavaType?

  fun resolveTypeOrThrow(node: TypeNode): JavaType

  fun findFieldOrThrow(name: String, token: LexToken): MarcelField {
    return findField(name) ?: throw MarcelSemanticException(token, "Variable $name is not defined")
  }
  fun findField(name: String): MarcelField?

  fun hasLocalVariable(name: String): Boolean {
    return findLocalVariable(name) != null
  }
  fun findLocalVariable(name: String): LocalVariable?

  fun dispose() {}
}