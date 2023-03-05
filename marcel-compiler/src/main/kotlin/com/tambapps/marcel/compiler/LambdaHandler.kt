package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.util.javaType
import com.tambapps.marcel.parser.MethodParameter
import com.tambapps.marcel.parser.ast.AstNodeTypeResolver
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.ConstructorNode
import com.tambapps.marcel.parser.ast.FieldNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.MethodParameterNode
import com.tambapps.marcel.parser.ast.expression.ConstructorCallNode
import com.tambapps.marcel.parser.ast.expression.FieldAssignmentNode
import com.tambapps.marcel.parser.ast.expression.FunctionBlockNode
import com.tambapps.marcel.parser.ast.expression.FunctionCallNode
import com.tambapps.marcel.parser.ast.expression.GetFieldAccessOperator
import com.tambapps.marcel.parser.ast.expression.LambdaNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.SimpleFunctionCallNode
import com.tambapps.marcel.parser.ast.expression.SuperConstructorCallNode
import com.tambapps.marcel.parser.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.parser.ast.statement.StatementNode
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.scope.LambdaScope
import com.tambapps.marcel.parser.scope.MethodScope
import com.tambapps.marcel.parser.scope.Variable
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType
import marcel.lang.lambda.Lambda
import org.objectweb.asm.Opcodes
import java.util.LinkedHashSet

class LambdaHandler(private val classNode: ClassNode, private val typeResolver: JavaTypeResolver) {
  private var lambdasCount = 0

  fun defineLambda(lambdaNode: LambdaNode): ConstructorCallNode {
    val token = lambdaNode.token
    val scope = lambdaNode.scope
    val className = generateLambdaName(scope)
    val interfaceType = if (lambdaNode.interfaceType != null && !Lambda::class.javaType.isAssignableFrom(lambdaNode.interfaceType!!)) lambdaNode.interfaceType else null
    tryMatchParametersWithLambda(lambdaNode)
    val lambdaInterfaceType = AstNodeTypeResolver.getLambdaType(typeResolver, lambdaNode)
    val type = scope.typeResolver.defineClass(className, JavaType.Object, false,
        if (interfaceType != null) listOf(interfaceType, lambdaInterfaceType)
        else listOf(lambdaInterfaceType))
    val methods = mutableListOf<MethodNode>()
    val lambdaClassNode = ClassNode(token, scope.copy(type), Opcodes.ACC_PRIVATE, type, type.superType!!,
      false, methods, mutableListOf(), mutableListOf())

    // getting all referenced variables so that the lambda can access them
    val referencedLocalVariables = LinkedHashSet<Variable>()
    lambdaNode.blockNode.forEachNode {
      if (it is ReferenceExpression) {
        val variable = scope.parentScope.findVariable(it.name)
        if (variable != null && it.name != "it") referencedLocalVariables.add(variable)
      }
    }
    val fields = referencedLocalVariables.map {
      FieldNode(token, it.type, it.name, type, Opcodes.ACC_PRIVATE or Opcodes.ACC_FINAL, null)
    }
    lambdaClassNode.fields.addAll(fields)

    // creating constructor that will initialize all fields
    val constructorParameters = fields.map { MethodParameterNode(it.type, it.name, true) }

    // adding default constructor
    val constructorScope = MethodScope(lambdaClassNode.scope, JavaMethod.CONSTRUCTOR_NAME, constructorParameters, JavaType.void)
    val constructorBlock: MutableList<StatementNode> = fields.map {
      ExpressionStatementNode(token,
        FieldAssignmentNode(token, constructorScope, GetFieldAccessOperator(token, ReferenceExpression.thisRef(constructorScope),
          ReferenceExpression(token, constructorScope, it.name), false), ReferenceExpression(token, constructorScope, it.name))
      )
    }.toMutableList()
    constructorBlock.add(0, ExpressionStatementNode(token, SuperConstructorCallNode(token, constructorScope, mutableListOf())))
    lambdaClassNode.methods.add(ConstructorNode.of(lambdaClassNode, constructorScope, constructorParameters.toMutableList(),
      constructorBlock))

    // define lambda interfaceType method
    val lambdaMethod = scope.typeResolver.getDeclaredMethods(lambdaInterfaceType).first()
    val lambdaReturnType = lambdaInterfaceType.genericTypes.lastOrNull() ?: JavaType.Object
    var parameters = lambdaMethod.parameters.toMutableList()
    if (parameters.size == 1 && lambdaNode.parameters.isEmpty()) {
      parameters = mutableListOf(MethodParameter(lambdaMethod.parameters.first().type, lambdaMethod.parameters.first().type, "it"))
    } else if (lambdaNode.parameters.isNotEmpty()) {
      if (lambdaMethod.parameters.size != lambdaNode.parameters.size) {
        throw MarcelSemanticException(token, "Lambda method parameters count is not consistent")
      }
      for (i in lambdaMethod.parameters.indices) {
        val methodParameter = lambdaMethod.parameters[i]
        val nodeParameter = lambdaNode.parameters[i]
        if (!nodeParameter.type.isAssignableFrom(methodParameter.type)) {
          throw MarcelSemanticException(token, "Bad parameter lambda ${nodeParameter.type} ${nodeParameter.name}")
        }
        parameters[i] = nodeParameter
      }
    }

    val lambdaMethodScope = MethodScope(lambdaClassNode.scope, lambdaMethod.name, parameters, lambdaReturnType)
    val fblock = FunctionBlockNode(token, lambdaMethodScope, lambdaNode.blockNode.statements)
    fblock.setTreeScope(lambdaMethodScope)
    lambdaClassNode.addMethod(
      MethodNode(Opcodes.ACC_PUBLIC, type, lambdaMethod.name,
        fblock,
        parameters.map { MethodParameterNode(it) }.toMutableList(),
        lambdaReturnType, lambdaMethodScope,
        false)
    )

    // define the interface method if any
    if (interfaceType != null) {
      val declaredMethods = scope.typeResolver.getDeclaredMethods(interfaceType)
        .filter { it.isAbstract }
      if (declaredMethods.size != 1) {
        throw MarcelSemanticException(token, "Cannot make a lambda out of interface $interfaceType")
      }

      val interfaceMethod = declaredMethods.first()
      val interfaceMethodScope = MethodScope(lambdaClassNode.scope, interfaceMethod.name, parameters, interfaceMethod.returnType)


      val lambdaMethodCall = SimpleFunctionCallNode(token, interfaceMethodScope, lambdaMethod.name,
        parameters.map {
          ReferenceExpression(token, interfaceMethodScope, it.name)
        }.toMutableList(), ReferenceExpression.thisRef(interfaceMethodScope))

      lambdaClassNode.addMethod(
        MethodNode(Opcodes.ACC_PUBLIC, type, interfaceMethod.name,
          FunctionBlockNode(token, interfaceMethodScope, mutableListOf(ExpressionStatementNode(token, lambdaMethodCall))),
          parameters.map { MethodParameterNode(it) }.toMutableList(),
          interfaceMethod.returnType, interfaceMethodScope,
          false)
      )
    }
    lambdaClassNode.methods.forEach { scope.typeResolver.defineMethod(type, it) }
    lambdaClassNode.fields.forEach { scope.typeResolver.defineField(type, it) }
    // add lambda class as an inner class of the class it was defined in
    classNode.innerClasses.add(lambdaClassNode)

    return ConstructorCallNode(token, lambdaNode.scope, type,
      referencedLocalVariables.map { ReferenceExpression(token, scope.parentScope, it.name) }.toMutableList())
  }

  private fun tryMatchParametersWithLambda(lambdaNode: LambdaNode) {
    if (lambdaNode.explicit0Parameters || lambdaNode.parameters.isNotEmpty()) return
    val interfaceType = lambdaNode.interfaceType ?: return
    val method = typeResolver.getInterfaceLambdaMethod(interfaceType)
    lambdaNode.parameters.clear()
    lambdaNode.parameters.addAll(method.parameters)
    if (lambdaNode.parameters.size == 1) {
      val p = lambdaNode.parameters[0]
      lambdaNode.parameters[0] = MethodParameter(p.type, p.rawType, "it", p.isFinal)
    }
  }

  private fun generateLambdaName(scope: LambdaScope): String {
    val prefix = scope.classType.simpleName + "\$"
    return if (scope.parentScope is MethodScope) "${prefix}_" + (scope.parentScope as MethodScope).methodName + "_closure" + lambdasCount++
    else "${prefix}_" + scope.classType.simpleName + "_lambda" + lambdasCount++
  }
}