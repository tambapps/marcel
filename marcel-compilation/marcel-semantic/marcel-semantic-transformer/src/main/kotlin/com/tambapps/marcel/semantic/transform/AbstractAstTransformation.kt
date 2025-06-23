package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.transform.SemanticPurpose
import com.tambapps.marcel.semantic.transform.compose.AstNodeComposer
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.processor.cast.AstNodeCaster
import com.tambapps.marcel.semantic.processor.imprt.ImportResolver
import com.tambapps.marcel.semantic.processor.scope.ClassScope
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.symbol.type.JavaPrimitiveType

/**
 * Base class for AST transformations providing handy methods to handle/generate AST nodes
 */
abstract class AbstractAstTransformation : AstNodeComposer(LexToken.DUMMY, LexToken.DUMMY), SyntaxTreeTransformation {

  override lateinit var symbolResolver: MarcelSymbolResolver
  protected lateinit var caster: AstNodeCaster
  protected lateinit var purpose: SemanticPurpose


  override fun init(symbolResolver: MarcelSymbolResolver, purpose: SemanticPurpose) {
    this.symbolResolver = symbolResolver
    this.caster = AstNodeCaster(symbolResolver)
    this.purpose = purpose
  }

  protected fun classScope(classNode: ClassNode) = classScope(classNode.type)
  protected fun classScope(classType: JavaType) = ClassScope(symbolResolver, classType, null, ImportResolver.DEFAULT_IMPORTS)

  fun resolve(node: TypeCstNode): JavaType = currentScope.resolveTypeOrThrow(node)

  override fun cast(expectedType: JavaType, node: ExpressionNode) = caster.cast(expectedType, node)

  override fun castNumberConstantOrNull(value: Int, type: JavaPrimitiveType)= caster.castNumberConstantOrNull(value, type)

  override fun javaCast(expectedType: JavaType, node: ExpressionNode) = caster.javaCast(expectedType, node)

  override fun truthyCast(node: ExpressionNode) = caster.truthyCast(node)
}
