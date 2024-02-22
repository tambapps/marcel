package com.tambapps.marcel.semantic.scope

import com.tambapps.marcel.semantic.imprt.ImportResolver
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.variable.field.MarcelField

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

  override fun findField(name: String): MarcelField? {
    val fieldOwner = importResolver.resolveMemberOwnerType(name) ?: return null
    return symbolResolver.findField(fieldOwner, name)
  }

  override fun findLocalVariable(name: String) = null


}