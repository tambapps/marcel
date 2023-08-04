package com.tambapps.marcel.compiler.annotation

import com.tambapps.marcel.compiler.util.javaAnnotation
import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.ast.*
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
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
            val token = classNode.fields.find { it.name == "delegate" }!!.token
            val delegateField = classNode.fields.find { it.name == "delegate" }!!
            if (classNode.type.allImplementedInterfaces.find { it.raw() == DelegatedObject::class.javaType }!!.genericTypes.firstOrNull()?.let { it.isAssignableFrom(delegateField.type) } != false) {
                val getDelegateMethod = MethodNode.from(classNode.token, classScope = classNode.scope, ownerClass = classNode.type, name = "getDelegate", parameters = emptyList(),
                    returnType = delegateField.type.objectType, annotations = listOf(AnnotationNode(token, Override::class.javaAnnotation, emptyList())), staticContext = false
                )
                val scope = getDelegateMethod.scope
                getDelegateMethod.block.addStatement(
                    ReturnNode(
                            token, scope, GetFieldAccessOperator(
                            token, ReferenceExpression.thisRef(scope), ReferenceExpression(token, scope, "delegate"), nullSafe = false, directFieldAccess = true)))
                classNode.addMethod(getDelegateMethod)
            }
        }
    }
}