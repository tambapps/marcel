package com.tambapps.marcel.compiler.transform

import com.tambapps.marcel.parser.cst.CstNode
import com.tambapps.marcel.parser.cst.AnnotationCstNode as AnnotationCstNode
import com.tambapps.marcel.parser.cst.ClassCstNode as ClassCstNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.transform.SyntaxTreeTransformation
import com.tambapps.marcel.semantic.type.JavaAnnotationType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.SourceJavaType
import marcel.lang.MarcelSyntaxTreeTransformationClass
import java.lang.Exception
import java.lang.annotation.ElementType

class SyntaxTreeTransformer(
  private val typeResolver: JavaTypeResolver
) {

  private val map = mutableMapOf<JavaAnnotationType, List<SyntaxTreeTransformation>>()

  fun loadTransformations(semantic: MarcelSemantic) {
    semantic.cst.classes.forEach { doLoadTransformations(semantic, it) }
    semantic.cst.script?.let { doLoadTransformations(semantic, it) }
  }

  fun applyTransformations(node: ModuleNode) {
    if (map.isEmpty()) return // no need to iterate over everything if there aren't any transformations to apply
    for (classNode in node.classes) {
      applyTransformations(classNode)
    }
  }

  private fun applyTransformations(classNode: ClassNode) {
    classNode.annotations.forEach { annotationNode -> applyTransformations(classNode, classNode, annotationNode) }

    for (fieldNode in classNode.fields.toList()) { // copying to avoid concurrent modifications
      fieldNode.annotations.forEach { annotationNode -> applyTransformations(fieldNode, classNode, annotationNode) }
    }

    for (methodNode in classNode.methods.toList()) { // copying to avoid concurrent modifications
      methodNode.annotations.forEach { annotationNode -> applyTransformations(methodNode, classNode, annotationNode) }
    }

    for (innerClass in classNode.innerClasses.toList()) { // copying to avoid concurrent modifications
      applyTransformations(innerClass)
    }
  }

  private fun applyTransformations(node: AstNode, classNode: ClassNode, annotation: AnnotationNode) {
    map[annotation.type]?.forEach { transformation ->
      try {
        transformation.transform(node, classNode, annotation)
      } catch (e: Exception) {
        if (e !is MarcelSemanticException) System.err.println("Error while applying AST transformation ${transformation.javaClass} from annotation ${annotation.type}")
        throw e
      }
    }
  }

  private fun doLoadTransformations(semantic: MarcelSemantic, classNode: ClassCstNode) {
    val javaType = typeResolver.of(classNode.className) as SourceJavaType
    loadFromAnnotations(semantic, classNode, ElementType.TYPE, javaType, classNode.annotations)
    classNode.fields.forEach { fieldNode ->
      loadFromAnnotations(semantic, fieldNode, ElementType.FIELD, javaType, fieldNode.annotations)
    }
    classNode.methods.forEach { methodNode ->
      loadFromAnnotations(semantic, methodNode, ElementType.METHOD, javaType, methodNode.annotations)
    }
    classNode.innerClasses.forEach { doLoadTransformations(semantic, it) }
  }

  private fun loadFromAnnotations(semantic: MarcelSemantic,
                                  node: CstNode,
                                  elementType: ElementType,
                                  classType: SourceJavaType, annotations: List<AnnotationCstNode>) {
    annotations.asSequence()
      .map { semantic.visit(it, elementType) }
      .filter { annotation -> annotation.type.isLoaded
          && annotation.type.realClazz.getAnnotation(MarcelSyntaxTreeTransformationClass::class.java) != null }
      .forEach { annotation ->
        val transformations = map.computeIfAbsent(annotation.type, this::getTransformations)
        if (transformations.isNotEmpty()) {
          transformations.forEach { transformation ->
            transformation.init(typeResolver)
            try {
              transformation.transform(classType, node, annotation)
            } catch (e: Exception) {
              if (e !is MarcelSemanticException) System.err.println("Error while applying AST transformation ${transformation.javaClass} from annotation ${annotation.type}")
              throw e
            }
          }
        } else {
          map.remove(annotation.type)
        }
      }
  }

  private fun getTransformations(annotationType: JavaAnnotationType): List<SyntaxTreeTransformation> {
    val transformationAnnotation = annotationType.realClazz.getAnnotation(MarcelSyntaxTreeTransformationClass::class.java)
    val classes =
      if (transformationAnnotation.classes.isNotEmpty()) transformationAnnotation.classes.map { it.java }
      else transformationAnnotation.value.mapNotNull {
        try {
          Class.forName(it)
        } catch (e: ClassNotFoundException) {
          System.err.println("Couldn't find AST transformation class $it for annotation $annotationType")
          e.printStackTrace()
          null
        }
      }
        .toList()

    return classes.mapNotNull {
      try {
        it.getDeclaredConstructor().newInstance() as SyntaxTreeTransformation
      } catch (e: ReflectiveOperationException) {
        System.err.println("Error while attempting to instantiate AST Transformation $it for annotation $annotationType")
        e.printStackTrace()
        null
      }
    }
  }
}