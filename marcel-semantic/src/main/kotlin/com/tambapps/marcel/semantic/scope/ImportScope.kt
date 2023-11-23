package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver

/**
 * Base scope allowing to resolve types using imports
 */
class ImportScope(typeResolver: JavaTypeResolver, imports: List<ImportNode>,
  packageName: String?) :
  AbstractScope(typeResolver, packageName, imports) {

  // should not be used
  override val classType = JavaType.Object
  override val forExtensionType = null

  override fun findField(name: String) = null
  override fun findLocalVariable(name: String) = null


}