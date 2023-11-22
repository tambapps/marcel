package com.tambapps.marcel.compiler.check

import com.tambapps.marcel.compiler.ClassNodeVisitor
import com.tambapps.marcel.compiler.JavaTypeResolver
import com.tambapps.marcel.parser.ast.ClassNode
import com.tambapps.marcel.parser.ast.MethodNode
import com.tambapps.marcel.parser.ast.expression.AsNode
import com.tambapps.marcel.parser.ast.expression.ExpressionNode
import com.tambapps.marcel.parser.ast.expression.ReferenceExpression
import com.tambapps.marcel.parser.ast.expression.SimpleFunctionCallNode
import com.tambapps.marcel.parser.exception.MarcelSemanticLegacyException

/**
 * Check that all implemented interfaces methods are defined.
 */
internal class ImplementedInterfaceCheck: ClassNodeVisitor {

  override fun visit(classNode: ClassNode, typeResolver: JavaTypeResolver) {
    for (interfaze in classNode.type.directlyImplementedInterfaces) {
      for (interfaceMethod in typeResolver.getDeclaredMethods(interfaze).filter { it.isAbstract }) {
        val implementationMethod = classNode.methods.find { it.name == interfaceMethod.name
            && it.parameters.size == interfaceMethod.parameters.size
            && it.parametersAssignableTo(interfaceMethod)
        }

        if (implementationMethod == null || implementationMethod.isAbstract) {
          // maybe there is a generic implementation, in which case we have to generate the method with raw types
          throw MarcelSemanticLegacyException(classNode.token, "Class ${classNode.type} doesn't define method $interfaceMethod of interface $interfaze")
        }
        val rawInterfaceMethod = typeResolver.findMethod(interfaze.raw(), interfaceMethod.name, interfaceMethod.parameters, true, classNode)!!
        // we only need the match on parameters (== ignoring return type) because returning a more specific type is still a valid definition that doesn't need another implementation
        if (!rawInterfaceMethod.parameterMatches(implementationMethod)) {
          // need to write implementation method with raw type
          val rawMethodNode = MethodNode.fromJavaMethod(classNode.token, classNode.scope, rawInterfaceMethod)
          val rawParameterExpressions = mutableListOf<ExpressionNode>()
          for (i in rawMethodNode.parameters.indices) {
            rawParameterExpressions.add(
              AsNode(classNode.token,
              rawMethodNode.scope,
              implementationMethod.parameters[i].type,
              ReferenceExpression(classNode.token, rawMethodNode.scope, rawMethodNode.parameters[i].name)
            )
            )
          }
          rawMethodNode.block.addStatement(
            SimpleFunctionCallNode(classNode.token, rawMethodNode.scope, implementationMethod.name, rawParameterExpressions,
              ReferenceExpression.thisRef(rawMethodNode.scope))
          )
          classNode.methods.add(rawMethodNode)
        }
      }
    }
  }
}