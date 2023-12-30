package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.parser.cst.MethodCstNode
import com.tambapps.marcel.parser.cst.MethodParameterCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.CstNodeComposer
import com.tambapps.marcel.semantic.CstSemantic
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.method.JavaMethod
import com.tambapps.marcel.semantic.method.JavaMethodImpl
import com.tambapps.marcel.semantic.method.MethodParameter
import com.tambapps.marcel.semantic.scope.ImportScope
import com.tambapps.marcel.semantic.scope.Scope
import com.tambapps.marcel.semantic.type.JavaType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.NotLoadedJavaType
import com.tambapps.marcel.semantic.variable.field.JavaClassFieldImpl
import com.tambapps.marcel.semantic.variable.field.MarcelField
import com.tambapps.marcel.semantic.visitor.ImportCstNodeConverter

/**
 * Base class for CST transformations providing handy methods to handle/generate CST nodes
 */
abstract class AbstractCstTransformation : CstNodeComposer(), CstSemantic, SyntaxTreeTransformation {

  lateinit var typeResolver: JavaTypeResolver

  private var scope: ImportScope? = null

  override fun init(typeResolver: JavaTypeResolver) {
    this.typeResolver = typeResolver
  }

  final override fun transform(javaType: NotLoadedJavaType, node: CstNode, annotation: AnnotationNode) {
    scope = newScope(node)
    doTransform(javaType, node, annotation)
    scope = null
  }

  final override fun transform(node: AstNode, classNode: ClassNode, annotation: AnnotationNode) {
    // do nothing, as this method is for AST transformation
  }

  private fun newScope(node: CstNode): ImportScope {
    var cstNode: CstNode = node
    while (cstNode !is SourceFileCstNode) {
      if (node.parent == null) return ImportScope(typeResolver, Scope.DEFAULT_IMPORTS, null)
      cstNode = cstNode.parent!!
    }
    return ImportScope(typeResolver, Scope.DEFAULT_IMPORTS + ImportCstNodeConverter.convert(cstNode.imports), cstNode.packageName)
  }

  override fun toMethodParameter(
    ownerType: JavaType,
    forExtensionType: JavaType?,
    visibility: Visibility,
    isStatic: Boolean,
    parameterIndex: Int,
    methodName: String,
    node: MethodParameterCstNode
  ): MethodParameter {
    // we don't handle annotations nor defaultValue. Yup, CST transformations are limited
    return MethodParameter(
      resolve(node.type), node.name, emptyList(), null
    )
  }

  abstract fun doTransform(javaType: NotLoadedJavaType, node: CstNode, annotation: AnnotationNode)


  override fun resolve(node: TypeCstNode): JavaType = scope!!.resolveTypeOrThrow(node)

}
