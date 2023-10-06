package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.type.JavaType

class DynamicMethodField(
  type: JavaType,
  name: String,
  owner: JavaType,
  _getterMethod: JavaMethod?,
  _setterMethod: JavaMethod?
) : MethodField(type, name, owner, _getterMethod, _setterMethod, false)
