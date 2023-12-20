package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.AstNodeComposer
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
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


}
