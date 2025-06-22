package com.tambapps.marcel.semantic.symbol.variable.field

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType

class JavaClassFieldImpl(type: JavaType, name: String, owner: JavaType,
                         override val isFinal: Boolean,
                         override val visibility: Visibility,
                         override val isStatic: Boolean,
                         isSettable: Boolean = true
) : JavaClassField(type, name, owner, isSettable) {
}