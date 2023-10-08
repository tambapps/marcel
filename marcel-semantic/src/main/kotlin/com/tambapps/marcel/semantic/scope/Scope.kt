package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.ast.Ast2Node
import com.tambapps.marcel.semantic.ast.StaticImportNode
import com.tambapps.marcel.semantic.ast.WildcardImportNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTyped
import com.tambapps.marcel.semantic.variable.Variable
import marcel.lang.methods.DefaultMarcelStaticMethods

/**
 * The scope of a given node, holding data about variables and methods it can access
 */
interface Scope {

  companion object {
    val DEFAULT_IMPORTS = listOf(
      WildcardImportNode("java.lang"),
      WildcardImportNode("java.util"),
      WildcardImportNode("java.io"),
      WildcardImportNode("marcel.lang"),
      StaticImportNode(DefaultMarcelStaticMethods::class.java.name, "println")
    )
  }

  val classType: JavaType

  fun findMethodOrThrow(name: String, argumentTypes: List<JavaTyped>, node: CstNode): JavaMethod {
    // find first on class, then on imports, then on extensions
    return findMethod(name, argumentTypes) ?: throw MarcelSemanticException(node.token, "Method $name with parameters ${argumentTypes.map { it.type }} is not defined")
  }

  fun findMethod(name: String, argumentTypes: List<JavaTyped>): JavaMethod?

  fun findVariableOrThrow(name: String, token: LexToken): Variable {
    return findVariable(name) ?: throw MarcelSemanticException(token, "Variable $name is not defined")
  }

  fun hasVariable(name: String): Boolean {
    return findVariable(name) != null
  }

  fun findVariable(name: String): Variable?
}