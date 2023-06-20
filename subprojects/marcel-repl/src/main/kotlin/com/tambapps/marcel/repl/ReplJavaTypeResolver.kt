package com.tambapps.marcel.repl

import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.scope.BoundField
import com.tambapps.marcel.parser.scope.MarcelField
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Binding
import marcel.lang.MarcelClassLoader
import marcel.lang.Script

class ReplJavaTypeResolver constructor(classLoader: MarcelClassLoader?, private val binding: Binding) : JavaTypeResolver(classLoader) {

    private val scriptVariables = mutableMapOf<String, BoundField>()
    private val _libraryClasses = mutableListOf<ClassNode>()
    val libraryClasses: List<ClassNode> get() = _libraryClasses

    fun registerLibraryClass(classNode: ClassNode) {
        registerClass(classNode)
        _libraryClasses.add(classNode)
    }

    fun setScriptVariable(name: String, value: Any, type: Class<*>? = null) {
        scriptVariables[name] = BoundField(JavaType.of(type ?: value.javaClass), name, Script::class.javaType)
        binding.setVariable(name, value)
    }

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

    override fun getDeclaredFields(javaType: JavaType): List<MarcelField> {
        val fields = super.getDeclaredFields(javaType).toMutableList()
        if (isScript(javaType)) {
            fields.addAll(scriptVariables.values)
        }
        return fields
    }

    private fun isScript(javaType: JavaType) = Script::class.javaType.isAssignableFrom(javaType) && javaType.isTopLevel
}