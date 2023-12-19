package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.semantic.SemanticHelper
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.MethodNode
import com.tambapps.marcel.semantic.ast.cast.AstNodeCaster
import com.tambapps.marcel.semantic.ast.expression.ExpressionNode
import com.tambapps.marcel.semantic.ast.expression.literal.StringConstantNode
import com.tambapps.marcel.semantic.ast.statement.BlockStatementNode
import com.tambapps.marcel.semantic.ast.statement.ReturnStatementNode
import com.tambapps.marcel.semantic.ast.statement.StatementNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.JavaMethodImpl
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.visitor.AllPathsReturnVisitor

abstract class AbstractAstTransformation : AstTransformation {

  protected lateinit var typeResolver: JavaTypeResolver private set
  protected lateinit var caster: AstNodeCaster private set

  final override fun init(typeResolver: JavaTypeResolver) {
    this.typeResolver = typeResolver
    this.caster = AstNodeCaster(typeResolver)
  }

  protected fun signature(
    ownerClass: JavaType,
    visibility: Visibility = Visibility.PUBLIC,
    name: String,
    parameters: List<MethodParameter> = emptyList(),
    returnType: JavaType,
    isDefault: Boolean = false,
    isAbstract: Boolean = false,
    isStatic: Boolean = false,
    isConstructor: Boolean = false
  ): JavaMethod {
    return JavaMethodImpl(ownerClass, visibility, name, parameters, returnType, isDefault, isAbstract, isStatic, isConstructor)
  }

  protected fun methodNode(
    ownerClass: JavaType,
    visibility: Visibility = Visibility.PUBLIC,
    name: String,
    parameters: List<MethodParameter> = emptyList(),
    returnType: JavaType,
    statements: MutableList<StatementNode>,
    isDefault: Boolean = false,
    isAbstract: Boolean = false,
    isStatic: Boolean = false,
  ): MethodNode {
    val methodNode = MethodNode(name, parameters.toMutableList(), visibility, returnType, isStatic, LexToken.DUMMY, LexToken.DUMMY, ownerClass)
    methodNode.blockStatement = BlockStatementNode(statements, LexToken.DUMMY, LexToken.DUMMY)

    if (!AllPathsReturnVisitor.test(statements) && returnType == JavaType.void) {
      statements.add(SemanticHelper.returnVoid(methodNode))
    }
    return methodNode
  }

  protected fun returnStatement(expr: ExpressionNode, methodReturnType: JavaType): StatementNode {
    return ReturnStatementNode(
      caster.cast(methodReturnType, expr)
    )
  }

  protected fun string(value: String) = StringConstantNode(value, LexToken.DUMMY, LexToken.DUMMY)

}
