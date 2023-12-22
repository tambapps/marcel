package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.AstNodeComposer
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.scope.ClassScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver

/**
 * Base class for AST transformations providing handy methods to handle/generate AST nodes
 */
abstract class AbstractAstTransformation : AstNodeComposer(), AstTransformation {

  override lateinit var typeResolver: JavaTypeResolver
  override lateinit var caster: AstNodeCaster

  override fun init(typeResolver: JavaTypeResolver) {
    this.typeResolver = typeResolver
    this.caster = AstNodeCaster(typeResolver)
  }

  protected fun classScope(classNode: ClassNode) = classScope(classNode.type)
  protected fun classScope(classType: JavaType) = ClassScope(typeResolver, classType, null, Scope.DEFAULT_IMPORTS)
}
