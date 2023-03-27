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
class ReplJavaTypeResolver(classLoader: MarcelClassLoader?, private val binding: Binding) : JavaTypeResolver(classLoader) {

    private val scriptVariables = mutableMapOf<String, BoundField>()

    override fun defineField(javaType: JavaType, field: MarcelField) {
        if (field is BoundField) {
            // those fields are reserved for scripts
            scriptVariables[field.name] = field
        }
        super.defineField(javaType, field)
    }

    override fun findField(javaType: JavaType, name: String, declared: Boolean): MarcelField? {
        val f = super.findField(javaType, name, declared)
        if (f == null && Script::class.javaType.isAssignableFrom(javaType) && javaType.isTopLevel && binding.hasVariable(name)) {
            // if we're looking for a variable of a script, it may be a BoundField
            return scriptVariables[name]?.withOwner(javaType)
        }
        return f
    }
}