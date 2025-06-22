package com.tambapps.marcel.semantic.symbol.variable

import com.tambapps.marcel.semantic.symbol.variable.field.BoundField
import com.tambapps.marcel.semantic.symbol.variable.field.JavaClassField
import com.tambapps.marcel.semantic.symbol.variable.field.DynamicMethodField
import com.tambapps.marcel.semantic.symbol.variable.field.MarcelArrayLengthField
import com.tambapps.marcel.semantic.symbol.variable.field.CompositeField
import com.tambapps.marcel.semantic.symbol.variable.field.MethodField

interface VariableVisitor<T> {

  fun visit(variable: LocalVariable): T
  fun visit(variable: BoundField): T
  fun visit(variable: DynamicMethodField): T
  fun visit(variable: JavaClassField): T // same behaviour for ReflectJavaField and ClassField
  fun visit(variable: MarcelArrayLengthField): T
  fun visit(variable: CompositeField): T
  fun visit(variable: MethodField): T
}