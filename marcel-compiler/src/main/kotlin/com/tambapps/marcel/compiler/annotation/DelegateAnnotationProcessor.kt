package com.tambapps.marcel.compiler.annotation

import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.ReturnNode
import marcel.lang.Delegate
import marcel.lang.DelegatedObject

class DelegateAnnotationProcessor: FieldAnnotationProcessor {
    override val annotationType = Delegate::class.javaType

    override fun process(classNode: ClassNode, fieldNode: FieldNode) {
        if (!classNode.type.implements(DelegatedObject::class.javaType)) {
            classNode.type.addImplementedInterface(DelegatedObject::class.javaType.withGenericTypes(fieldNode.type.objectType))
        }
        if (classNode.type.implements(DelegatedObject::class.java.javaType)
            && !classNode.methods.any { it.name == "getDelegate" && it.parameters.isEmpty() }
            && classNode.fields.any { it.name == "delegate" }) {
            val delegateField = classNode.fields.find { it.name == "delegate" }!!
            if (classNode.type.allImplementedInterfaces.find { it.raw() == DelegatedObject::class.javaType }!!.genericTypes.firstOrNull()?.let { it.isAssignableFrom(delegateField.type) } != false) {
                val getDelegateMethod = MethodNode.from(classScope = classNode.scope, ownerClass = classNode.type, name = "getDelegate", parameters = emptyList(),
                    returnType = delegateField.type.objectType, annotations = listOf(AnnotationNode(LexToken.dummy(), Override::class.javaType)), staticContext = false
                )
                getDelegateMethod.block.addStatement(
                    ReturnNode(
                        LexToken.dummy(), getDelegateMethod.scope, ReferenceExpression(
                            LexToken.dummy(), getDelegateMethod.scope, delegateField.name)))
                classNode.addMethod(getDelegateMethod)
            }
        }
    }
}