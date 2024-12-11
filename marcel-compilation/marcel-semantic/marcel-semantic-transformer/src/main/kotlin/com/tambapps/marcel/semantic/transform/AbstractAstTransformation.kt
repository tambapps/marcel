package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.SemanticPurpose
import com.tambapps.marcel.semantic.compose.AstNodeComposer
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.imprt.ImportResolver
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

/**
 * Base class for AST transformations providing handy methods to handle/generate AST nodes
 */
abstract class AbstractAstTransformation : AstNodeComposer(LexToken.DUMMY, LexToken.DUMMY), SyntaxTreeTransformation {

  override lateinit var symbolResolver: MarcelSymbolResolver
  override lateinit var caster: AstNodeCaster
  protected lateinit var purpose: SemanticPurpose


  override fun init(symbolResolver: MarcelSymbolResolver, purpose: SemanticPurpose) {
    this.symbolResolver = symbolResolver
    this.caster = AstNodeCaster(symbolResolver)
    this.purpose = purpose
  }

  protected fun classScope(classNode: ClassNode) = classScope(classNode.type)
  protected fun classScope(classType: JavaType) = ClassScope(symbolResolver, classType, null, ImportResolver.DEFAULT_IMPORTS)

  fun resolve(node: TypeCstNode): JavaType = currentScope.resolveTypeOrThrow(node)

}
