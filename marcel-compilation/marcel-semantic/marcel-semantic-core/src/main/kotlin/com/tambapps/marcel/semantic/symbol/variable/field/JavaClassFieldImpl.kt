package com.tambapps.marcel.semantic.symbol.variable.field

import com.tambapps.marcel.semantic.symbol.Visibility
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.Nullness

class JavaClassFieldImpl constructor(type: JavaType, name: String, owner: JavaType,
                         override val nullness: Nullness,
                         override val isFinal: Boolean,
                         override val visibility: Visibility,
                         override val isStatic: Boolean,
                         isSettable: Boolean = true
) : JavaClassField(type, name, owner, isSettable) {
}