package com.tambapps.marcel.semantic.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.semantic.CompilationPurpose
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.symbol.MarcelSymbolResolver
import com.tambapps.marcel.semantic.type.SourceJavaType

/**
 * Syntax tree transformation
 */
interface SyntaxTreeTransformation {
  /**
   * Initialize this transformation
   *
   * @param symbolResolver the [MarcelSymbolResolver] used when compiling source code
   * @param purpose the [CompilationPurpose] of the compilation
   */
  fun init(symbolResolver: MarcelSymbolResolver, purpose: CompilationPurpose)

  /**
   * Transform the [JavaType][com.tambapps.marcel.semantic.type.JavaType] and/or the Concrete Syntax Tree
   * BEFORE having performed the semantic analysis
   *
   * @param javaType the type of the class containing the annotated node
   * @param node the annotated CST node
   * @param annotation the AST annotation node
   */
  fun transform(javaType: SourceJavaType, node: CstNode, annotation: AnnotationNode)

  /**
   * Transform the Abstract Syntax Tree AFTER having performed the semantic analysis
   *
   * @param node the annotated AST node
   * @param classNode the AST class node of the annotated AST node
   * @param annotation the AST annotation node
   */
  fun transform(node: AstNode, classNode: ClassNode, annotation: AnnotationNode)
}
