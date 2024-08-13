package com.tambapps.marcel.semantic.method


/**
 * Parent class to use for methods that are actually not java methods (e.g. cast<CastType>())
 */
abstract class VirtualMarcelMethod: AbstractMethod() {
  override val isConstructor = false
  override val isAbstract = false
  override val isDefault = false
  override val isAsync = false
  override val asyncReturnType = null
  override val isExtension = false
  override val isGetter = false
  override val isSetter = false
  override val isInline = false

}