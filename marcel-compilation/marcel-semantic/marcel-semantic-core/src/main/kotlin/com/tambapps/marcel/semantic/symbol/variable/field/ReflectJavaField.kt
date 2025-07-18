package com.tambapps.marcel.semantic.symbol.variable.field

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * A Field loaded from Java reflection API
 */
class ReflectJavaField private constructor(
  override val type: JavaType,
  override val name: String,
  override val owner: JavaType,
  override val nullness: Nullness,
  access: Int
) : JavaClassField(type, name, owner) {
  constructor(field: Field): this(JavaType.of(field.type), field.name, JavaType.of(field.declaringClass),
    Nullness.of(field),
    field.modifiers)

  override val visibility = Visibility.fromAccess(access)
  override val isStatic = (Modifier.STATIC and access) != 0
  override val isFinal = (Modifier.FINAL and access) != 0
}
