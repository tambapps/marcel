package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.semantic.type.JavaTypeResolver

abstract class AbstractAstTransformation : AstTransformation {

  private var typeResolver: JavaTypeResolver? = null

  final override fun init(typeResolver: JavaTypeResolver) {
    this.typeResolver = typeResolver
  }

}
