package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.FieldCstNode
import com.tambapps.marcel.parser.cst.SourceFileCstNode
import com.tambapps.marcel.parser.cst.TypeCstNode
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
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
abstract class AbstractCstTransformation : SyntaxTreeTransformation {

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

  abstract fun doTransform(javaType: NotLoadedJavaType, node: CstNode, annotation: AnnotationNode)

  protected fun toMarcelField(ownerType: JavaType, fieldNode: FieldCstNode): MarcelField {
    //val scope = ImportScope(typeResolver, fieldNode.parentClassNode.parentSourceFileNode.imports,)
    return JavaClassFieldImpl(
      resolve(fieldNode.type), fieldNode.name, ownerType, fieldNode.access.isFinal,
      Visibility.fromTokenType(fieldNode.access.visibility), fieldNode.access.isStatic)
  }

  fun resolve(node: TypeCstNode): JavaType = scope!!.resolveTypeOrThrow(node)

}
