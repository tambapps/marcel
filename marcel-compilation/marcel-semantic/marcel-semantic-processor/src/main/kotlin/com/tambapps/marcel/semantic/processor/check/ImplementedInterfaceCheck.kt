package com.tambapps.marcel.semantic.processor.check

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.FunctionCallNode
import com.tambapps.marcel.semantic.ast.expression.JavaCastNode
import com.tambapps.marcel.semantic.ast.expression.ReferenceNode
import com.tambapps.marcel.semantic.ast.expression.ThisReferenceNode
import com.tambapps.marcel.semantic.ast.expression.literal.VoidExpressionNode
import com.tambapps.marcel.semantic.ast.statement.ExpressionStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.processor.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.processor.visitor.ClassNodeVisitor
import com.tambapps.marcel.semantic.type.JavaType

/**
 * Check that all implemented interfaces methods are defined.
 */
internal object ImplementedInterfaceCheck : ClassNodeVisitor {

  override fun visit(classNode: ClassNode, symbolResolver: MarcelSymbolResolver) {
    for (interfaze in classNode.type.directlyImplementedInterfaces) {
      for (interfaceMethod in symbolResolver.getDeclaredMethods(interfaze)
        .filter { it.isAbstract && it.name != "equals" && it.name != "hashCode" }) {
        val implementationMethod =
          // sometimes some generated class methods are only defined in the node but not on the symbol resolver
           classNode.methods.find {
            it.name == interfaceMethod.name
                && it.parameters.size == interfaceMethod.parameters.size
                && it.parametersAssignableTo(interfaceMethod)
          }
             ?: symbolResolver.findMethod(classNode.type, interfaceMethod.name, interfaceMethod.parameters, excludeInterfaces = true)


        if (implementationMethod == null || implementationMethod.isAbstract) {
          // maybe there is a generic implementation, in which case we have to generate the method with raw types
          throw MarcelSemanticException(
            classNode.token,
            "Class ${classNode.type} doesn't define method $interfaceMethod of interface $interfaze"
          )
        }
        val rawInterfaceMethod = symbolResolver.findMethod(
          interfaze.raw(),
          interfaceMethod.name,
          interfaceMethod.parameters,
          true,
          classNode.token
        )!!
        // we only need the match on parameters (== ignoring return type) because returning a more specific type is still a valid definition that doesn't need another implementation
        if (!rawInterfaceMethod.parameterMatches(implementationMethod)) {
          // need to write implementation method with raw type
          val rawMethodNode = MethodNode.fromJavaMethod(rawInterfaceMethod, classNode.tokenStart, classNode.tokenEnd)
          val rawParameterArguments = mutableListOf<ExpressionNode>()
          for (i in rawMethodNode.parameters.indices) {
            rawParameterArguments.add(
              JavaCastNode(
                implementationMethod.parameters[i].type,
                ReferenceNode(
                  null,
                  rawMethodNode.toLocalVariable(rawMethodNode.parameters[i]),
                  classNode.token
                ),
                classNode.token
              )
            )
          }
          val fCall = FunctionCallNode(
            javaMethod = implementationMethod,
            owner = ThisReferenceNode(classNode.type, classNode.token),
            arguments = rawParameterArguments,
            tokenStart = classNode.token,
            tokenEnd = LexToken.DUMMY
          )
          if (rawInterfaceMethod.returnType != JavaType.void) {
            rawMethodNode.blockStatement.statements.add(
              ReturnStatementNode(fCall, fCall.tokenStart, fCall.tokenEnd)
            )
          } else {
            rawMethodNode.blockStatement.statements.add(
              ExpressionStatementNode(fCall, fCall.tokenStart, fCall.tokenEnd)
            )
            rawMethodNode.blockStatement.statements.add(
              ReturnStatementNode(VoidExpressionNode(fCall))
            )
          }
          classNode.methods.add(rawMethodNode)
        }
      }
    }
  }

  private fun findImplementationMethod() {

  }
}