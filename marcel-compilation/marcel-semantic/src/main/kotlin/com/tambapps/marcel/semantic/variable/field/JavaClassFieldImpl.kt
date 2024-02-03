package com.tambapps.marcel.semantic.variable.field

import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.type.JavaType

class JavaClassFieldImpl(type: JavaType, name: String, owner: JavaType,
                         override val isFinal: Boolean,
                         override val visibility: Visibility,
                         override val isStatic: Boolean,
                         isSettable: Boolean = true
) : JavaClassField(type, name, owner, isSettable) {
}