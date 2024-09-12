package com.tambapps.marcel.android.marshell.repl

import com.tambapps.marcel.repl.ReplMarcelSymbolResolver
import com.tambapps.marcel.semantic.extensions.javaType
import marcel.lang.MarcelClassLoader
import marcel.lang.extensions.MdStringExtensions

class MarshellSymbolResolver(classLoader: MarcelClassLoader?) : ReplMarcelSymbolResolver(classLoader) {
    init {
      loadExtensionUnsafe(MdStringExtensions::class.javaType)
    }
}