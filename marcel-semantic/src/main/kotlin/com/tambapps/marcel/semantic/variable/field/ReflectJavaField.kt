package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * A Field loaded from Java reflection API
 */
class ReflectJavaField private constructor(
  override val type: JavaType,
  override val name: String,
  override val owner: JavaType,
  access: Int
) : ClassField(type, name, owner) {
  constructor(field: Field): this(JavaType.of(field.type), field.name, JavaType.of(field.declaringClass), field.modifiers)

  override val visibility = Visibility.fromAccess(access)
  override val isStatic = (Modifier.STATIC and access) != 0
  override val isFinal = (Modifier.FINAL and access) != 0
}
