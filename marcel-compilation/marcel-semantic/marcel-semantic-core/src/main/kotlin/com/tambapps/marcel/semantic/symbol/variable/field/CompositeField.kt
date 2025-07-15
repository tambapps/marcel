package com.tambapps.marcel.semantic.symbol.variable.field

import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import com.tambapps.marcel.semantic.symbol.variable.Variable
import com.tambapps.marcel.semantic.symbol.variable.VariableVisitor
import java.util.Comparator
import java.util.TreeSet

/**
 * A field regrouping different kinds of Variable for a same name
 */
open class CompositeField(override val name: String): MarcelField {

  companion object {
    // we want method fields to come first, as we can just use direct field access for java fields
    private val fieldComparator: Comparator<MarcelField> = compareBy { it !is MethodField }
  }
  override fun <T> accept(visitor: VariableVisitor<T>) = visitor.visit(this)

  val classField: JavaClassField? get() = getters.firstNotNullOfOrNull { it as? JavaClassField }
  private val _getters = TreeSet<MarcelField>(fieldComparator)
  private val _setters = TreeSet<MarcelField>(fieldComparator)
  val getters: Set<MarcelField> = _getters
  val setters: Set<MarcelField> = _setters

  override val isSettable get() = _setters.isNotEmpty()
  override val isGettable get() = _getters.isNotEmpty()

  override val isFinal get() = (getters + setters).all { it.isFinal }
  override val isStatic get() = (getters + setters).all { it.isStatic }
  override val isMarcelStatic get() = (getters + setters).all { it.isMarcelStatic }
  override val owner get() = (getters.firstOrNull() ?: setters.first()).owner
  override val visibility get() = (getters + setters).map { it.visibility }.maxBy { it.ordinal }
  override val nullness: Nullness
    get() {
      if (getters.isEmpty() && setters.isEmpty()) return Nullness.UNKNOWN
      val all = getters + setters
      return when {
        all.all { it.nullness == Nullness.NOT_NULL } -> Nullness.NOT_NULL
        all.any { it.nullness == Nullness.NULLABLE } -> Nullness.NULLABLE
        else -> Nullness.UNKNOWN
      }
    }

  override fun isVisibleFrom(javaType: JavaType, access: Variable.Access): Boolean = when (access) {
    Variable.Access.GET -> getters.any { it.isVisibleFrom(javaType, access) }
    Variable.Access.SET -> setters.any { it.isVisibleFrom(javaType, access) }
    Variable.Access.ANY -> isVisibleFrom(javaType, Variable.Access.GET) || isVisibleFrom(javaType, Variable.Access.SET)
  }

  override val type get() =  JavaType.commonType((getters + setters).map { it.type })

  constructor(fields: Collection<MarcelField>): this(fields.first().name) {
    fields.forEach { mergeWith(it) }
  }

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

  fun removeGetter(methodField: MethodField) {
    _getters.remove(methodField)
  }

  fun removeSetter(methodField: MethodField) {
    _setters.remove(methodField)
  }

  fun mergeWith(field: MarcelField) {
    if (field.isGettable) addGetter(field)
    if (field.isSettable) addSetter(field)
  }

  fun mergeWith(other: CompositeField) {
    _getters.addAll(other._getters)
    _setters.addAll(other._setters)
  }

  fun isEmpty() = getters.isEmpty() && setters.isEmpty()
  fun settableFieldFrom(javaType: JavaType) = setters.find { it.isVisibleFrom(javaType, Variable.Access.SET) }
  fun gettableFieldFrom(javaType: JavaType) = getters.find { it.isVisibleFrom(javaType, Variable.Access.GET) }

}
