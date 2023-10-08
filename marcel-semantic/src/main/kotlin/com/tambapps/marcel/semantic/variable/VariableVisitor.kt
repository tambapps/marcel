package com.tambapps.marcel.semantic.variable

import com.tambapps.marcel.semantic.variable.field.BoundField
import com.tambapps.marcel.semantic.variable.field.JavaClassField
import com.tambapps.marcel.semantic.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.variable.field.MethodField

interface VariableVisitor<T> {

  fun visit(variable: LocalVariable): T
  fun visit(variable: BoundField): T
  fun visit(variable: DynamicMethodField): T
  fun visit(variable: JavaClassField): T // same behaviour for ReflectJavaField and ClassField
  fun visit(variable: MarcelArrayLengthField): T
  fun visit(variable: MarcelField): T
  fun visit(variable: MethodField): T
}