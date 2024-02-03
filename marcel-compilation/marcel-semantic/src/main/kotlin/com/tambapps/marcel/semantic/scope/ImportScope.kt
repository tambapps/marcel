package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.ast.ImportNode
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

/**
 * Base scope allowing to resolve types using imports
 */
class ImportScope(symbolResolver: MarcelSymbolResolver, imports: List<ImportNode>,
                  packageName: String?) :
  AbstractScope(symbolResolver, packageName, imports) {

  // should not be used
  override val classType = JavaType.Object
  override val forExtensionType = null

  override fun findField(name: String) = null
  override fun findLocalVariable(name: String) = null


}