package com.tambapps.marcel.repl.semantic

import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.expression.FunctionCallCstNode
import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.processor.SourceFileSemanticProcessor
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.processor.imprt.MutableImportResolver
import com.tambapps.marcel.semantic.symbol.type.JavaType
import marcel.lang.Script

class SourceFileReplSemanticProcessor(
  private val replSymbolResolver: ReplMarcelSymbolResolver,
  cst: SourceFileCstNode,
  fileName: String,
  imports: MutableImportResolver
) : SourceFileSemanticProcessor(replSymbolResolver, Script::class.javaType, cst, fileName) {

  init {
    this.imports.add(imports)
  }

  override fun resolveMethodCall(
    node: FunctionCallCstNode,
    positionalArguments: List<com.tambapps.marcel.semantic.ast.expression.ExpressionNode>,
    namedArguments: List<Pair<String, com.tambapps.marcel.semantic.ast.expression.ExpressionNode>>,
    castType: JavaType?
  ): com.tambapps.marcel.semantic.ast.expression.ExpressionNode? {
    val e = super.resolveMethodCall(node, positionalArguments, namedArguments, castType)
    if (e != null) return e

    // handle delegate function call
    val delegateVariable = getDelegate() ?: return null

    val methodResolve = methodResolver.resolveMethod(node, delegateVariable.type, node.value, positionalArguments, namedArguments)

    if (methodResolve != null && !methodResolve.first.isMarcelStatic) {
      val owner = ReferenceNode(variable = delegateVariable, token = node.token, owner = ThisReferenceNode(currentScope.classType, node.token))
      return fCall(methodResolve = methodResolve, owner = owner, castType = castType,
        tokenStart = node.tokenStart,
        tokenEnd = node.tokenEnd)
    }
    return null
  }

  private fun getDelegate() = replSymbolResolver.getBoundField("delegate")
}