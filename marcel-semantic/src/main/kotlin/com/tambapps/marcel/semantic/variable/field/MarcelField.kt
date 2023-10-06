package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.variable.Variable

/**
 * A field regrouping different kinds of Variable for a same name
 */
open class MarcelField(override val name: String): Variable {
  val classField: ClassField? get() = getters.firstNotNullOfOrNull { it as? ClassField }
  private val _getters = mutableSetOf<JavaField>()
  private val _setters = mutableSetOf<JavaField>()
  val getters: Set<JavaField> = _getters
  val setters: Set<JavaField> = _setters

  override val isFinal get() = (getters + setters).all { it.isFinal }
  val isStatic get() = (getters + setters).all { it.isStatic }
  override fun isAccessibleFrom(javaType: JavaType) = (getters + setters).any { it.isAccessibleFrom(javaType) }

  override val type get() =  JavaType.commonType((getters + setters).map { it.type })

  constructor(field: JavaField): this(field.name) {
    mergeWith(field)
  }
  fun addGetter(javaField: JavaField) {
    // if we try to add an extension method for a class but the method was already defined for this class (e.g. IntList.last vs List.last), we ignore the extension
    if (javaField is MethodField && javaField.isExtension && _getters.isNotEmpty()) return
    _getters.add(javaField)
  }
  fun addSetter(javaField: JavaField) {
    // if we try to add an extension method for a class but the method was already defined for this class (e.g. IntList.last vs List.last), we ignore the extension
    if (javaField is MethodField && javaField.isExtension && _setters.isNotEmpty()) return
    _setters.add(javaField)
  }

  fun mergeWith(field: JavaField) {
    if (field.isGettable) addGetter(field)
    if (field.isSettable) addSetter(field)
  }

  fun mergeWith(other: MarcelField) {
    _getters.addAll(other._getters)
    _setters.addAll(other._setters)
  }

  fun settableFieldFrom(javaType: JavaType) = setters.find { it.isAccessibleFrom(javaType) }
  fun gettableFieldFrom(javaType: JavaType) = getters.find { it.isAccessibleFrom(javaType) }
}
