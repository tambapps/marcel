package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.scope.BoundField
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.Script

// TODO keep method nodes here
class ReplJavaTypeResolver(classLoader: MarcelClassLoader?, val binding: Binding) : JavaTypeResolver(classLoader) {

    override fun findField(javaType: JavaType, name: String, declared: Boolean): MarcelField? {
        val f = super.findField(javaType, name, declared)
        if (f != null || javaType.implements(Script::class.javaType) && javaType.isTopLevel && binding.hasVariable(name)) {
            val value = binding.getVariable<Any>(name)
            val type = value?.javaClass?.javaType ?: JavaType.Object
            return BoundField(type, name)
        }
        return null
    }
}