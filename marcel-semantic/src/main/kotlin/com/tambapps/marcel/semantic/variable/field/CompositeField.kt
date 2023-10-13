package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.Variable
import com.tambapps.marcel.semantic.variable.VariableVisitor

/**
 * A field regrouping different kinds of Variable for a same name
 */
open class CompositeField(override val name: String): Variable {

  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)

  val classField: JavaClassField? get() = getters.firstNotNullOfOrNull { it as? JavaClassField }
  private val _getters = mutableSetOf<MarcelField>()
  private val _setters = mutableSetOf<MarcelField>()
  val getters: Set<MarcelField> = _getters
  val setters: Set<MarcelField> = _setters

  override val isSettable get() = _setters.isNotEmpty()
  override val isGettable get() = _getters.isNotEmpty()

  override val isFinal get() = (getters + setters).all { it.isFinal }
  val isStatic get() = (getters + setters).all { it.isStatic }
  override fun isAccessibleFrom(javaType: JavaType) = (getters + setters).any { it.isAccessibleFrom(javaType) }

  override val type get() =  JavaType.commonType((getters + setters).map { it.type })

  constructor(field: MarcelField): this(field.name) {
    mergeWith(field)
  }
  fun addGetter(marcelField: MarcelField) {
    // if we try to add an extension method for a class but the method was already defined for this class (e.g. IntList.last vs List.last), we ignore the extension
    if (marcelField is MethodField && marcelField.isExtension && _getters.isNotEmpty()) return
    _getters.add(marcelField)
  }
  fun addSetter(marcelField: MarcelField) {
    // if we try to add an extension method for a class but the method was already defined for this class (e.g. IntList.last vs List.last), we ignore the extension
    if (marcelField is MethodField && marcelField.isExtension && _setters.isNotEmpty()) return
    _setters.add(marcelField)
  }

  fun mergeWith(field: MarcelField) {
    if (field.isGettable) addGetter(field)
    if (field.isSettable) addSetter(field)
  }

  fun mergeWith(other: CompositeField) {
    _getters.addAll(other._getters)
    _setters.addAll(other._setters)
  }

  fun settableFieldFrom(javaType: JavaType) = setters.find { it.isAccessibleFrom(javaType) }
  fun gettableFieldFrom(javaType: JavaType) = getters.find { it.isAccessibleFrom(javaType) }
}
