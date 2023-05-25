package com.tambapps.marcel.compiler.annotation

import com.tambapps.marcel.compiler.ModuleNodeVisitor
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.FieldNode
import com.tambapps.marcel.parser.ast.ModuleNode
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Delegate

interface FieldAnnotationProcessor: ModuleNodeVisitor {
    val annotationType: JavaType

    override fun visit(moduleNode: ModuleNode) {
        val map = moduleNode.classes.associateWith { classNode ->
            classNode.fields.filter { it.getAnnotation(Delegate::class.javaType) != null }
        }
        map.forEach { (classNode, fields) ->
            process(classNode, fields)
        }
    }

    // useful to override for annotations that needs to group annotated fields by class when processing them
    fun process(classNode: ClassNode, fields: List<FieldNode>) {
        fields.forEach {
            process(classNode, it)
        }
    }

    fun process(classNode: ClassNode, fieldNode: FieldNode)
}