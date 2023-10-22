package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver

/**
 * Base scope allowing to resolve types using imports
 */
class ImportScope(typeResolver: JavaTypeResolver, imports: List<ImportNode>) :
  AbstractScope(typeResolver, JavaType.Object, imports) {
  override fun findField(name: String) = null
  override fun findLocalVariable(name: String) = null
}