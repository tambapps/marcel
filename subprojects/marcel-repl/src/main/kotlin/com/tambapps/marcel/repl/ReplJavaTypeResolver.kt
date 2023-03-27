package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.scope.BoundField
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.Script

class ReplJavaTypeResolver(classLoader: MarcelClassLoader?, private val binding: Binding) : JavaTypeResolver(classLoader) {

    private val scriptVariables = mutableMapOf<String, BoundField>()

    override fun defineField(javaType: JavaType, field: MarcelField) {
        super.defineField(javaType, field)
        if (isScript(javaType) && field is BoundField) {
            // those fields are reserved for scripts
            scriptVariables[field.name] = field
        }
    }

    override fun findField(javaType: JavaType, name: String, declared: Boolean): MarcelField? {
        val f = super.findField(javaType, name, declared)
        if (f == null && isScript(javaType) && binding.hasVariable(name)) {
            // if we're looking for a variable of a script, it may be a BoundField
            return scriptVariables[name]?.withOwner(javaType)
        }
        return f
    }

    private fun isScript(javaType: JavaType) = Script::class.javaType.isAssignableFrom(javaType) && javaType.isTopLevel
}