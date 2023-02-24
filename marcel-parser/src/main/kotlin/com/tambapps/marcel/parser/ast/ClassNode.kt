package com.tambapps.marcel.parser.ast

import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.asm.AsmUtils
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Scope
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.Binding
import org.objectweb.asm.Opcodes

class ClassNode constructor(val scope: Scope, val access: Int, val type: JavaType, val superType: JavaType,
                            val isScript: Boolean,
                            val methods: MutableList<MethodNode>,
                            val fields: MutableList<FieldNode>,
                            val innerClasses: MutableList<ClassNode>): AstNode {

  var staticInitializationNode: StaticInitializationNode? = null

  init {
    if (isScript) {
      methods.add(scriptEmptyConstructor())
      methods.add(scriptBindingConstructor())
    }
  }
  fun getOrInitStaticInitializationNode(): StaticInitializationNode {
    if (staticInitializationNode == null) {
      staticInitializationNode = StaticInitializationNode.newInstance(this)
    }
    return staticInitializationNode!!
  }
  val constructorsCount: Int
    get() = methods.count { it.name == JavaMethod.CONSTRUCTOR_NAME }
  val constructors: List<ConstructorNode>
    get() = methods.mapNotNull { it as? ConstructorNode }

  val internalName = AsmUtils.getInternalName(type)
  fun addMethod(method: MethodNode) {
    if (methods.any { it.matches(scope.typeResolver, method.name, method.parameters) }) {
      throw MarcelSemanticException("Cannot have two methods with the same name")
    }
    methods.add(method)
  }

  private fun scriptEmptyConstructor(): ConstructorNode {
    val emptyConstructorScope = MethodScope(scope, JavaMethod.CONSTRUCTOR_NAME, emptyList(), JavaType.void)
    return ConstructorNode(
      Opcodes.ACC_PUBLIC,
      FunctionBlockNode(emptyConstructorScope, mutableListOf()),
      mutableListOf(),
      emptyConstructorScope
    )
  }
  private fun scriptBindingConstructor(): ConstructorNode {
    val bindingType = JavaType.of(Binding::class.java)
    val bindingParameterName = "binding"
    val bindingConstructorParameters = mutableListOf(MethodParameter(bindingType, bindingParameterName))
    val bindingConstructorScope = MethodScope(scope, JavaMethod.CONSTRUCTOR_NAME, bindingConstructorParameters, JavaType.void)
    return ConstructorNode(
      Opcodes.ACC_PUBLIC, FunctionBlockNode(bindingConstructorScope, mutableListOf(
        ExpressionStatementNode(
          SuperConstructorCallNode(scope, mutableListOf(
            ReferenceExpression(
              bindingConstructorScope, bindingParameterName)
          ))
        )
      )), bindingConstructorParameters, bindingConstructorScope
    )
  }

  override fun toString(): String {
    return "class $type {\n" + methods.joinToString(separator = "\n", transform = { "  $it" }) + "\n}"
  }
}