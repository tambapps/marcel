package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.compose.AstNodeComposer
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

/**
 * Base class for AST transformations providing handy methods to handle/generate AST nodes
 */
abstract class AbstractAstTransformation : AstNodeComposer(), SyntaxTreeTransformation {

  override lateinit var symbolResolver: MarcelSymbolResolver
  override lateinit var caster: AstNodeCaster


  override fun init(symbolResolver: MarcelSymbolResolver) {
    this.symbolResolver = symbolResolver
    this.caster = AstNodeCaster(symbolResolver)
  }

  protected fun classScope(classNode: ClassNode) = classScope(classNode.type)
  protected fun classScope(classType: JavaType) = ClassScope(symbolResolver, classType, null, Scope.DEFAULT_IMPORTS)

  fun resolve(node: TypeCstNode): JavaType = currentScope.resolveTypeOrThrow(node)

}
