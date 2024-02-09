package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.imprt.ImportResolver
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver

/**
 * Base scope allowing to resolve types using imports
 */
class ImportScope(
  symbolResolver: MarcelSymbolResolver,
  importResolver: ImportResolver,
  packageName: String?
) :
  AbstractScope(symbolResolver, packageName, importResolver) {

  // should not be used
  override val classType = JavaType.Object
  override val forExtensionType = null

  override fun findField(name: String) = null
  override fun findLocalVariable(name: String) = null


}