package com.tambapps.marcel.compiler.transform

import com.tambapps.marcel.parser.cst.AnnotationNode as AnnotationCstNode
import com.tambapps.marcel.parser.cst.ClassNode as ClassCstNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.ast.AnnotationNode
import com.tambapps.marcel.semantic.ast.AstNode
import com.tambapps.marcel.semantic.ast.ClassNode
import com.tambapps.marcel.semantic.ast.ModuleNode
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.transform.AstTransformation
import com.tambapps.marcel.semantic.type.JavaAnnotationType
import com.tambapps.marcel.semantic.type.JavaTypeResolver
import com.tambapps.marcel.semantic.type.NotLoadedJavaType
import marcel.lang.MarcelAstTransformationClass
import java.lang.Exception
import java.lang.annotation.ElementType

class AstTransformer(
  private val typeResolver: JavaTypeResolver
) {

  private val map = mutableMapOf<JavaAnnotationType, List<AstTransformation>>()

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
    classNode.annotations.forEach { annotationNode -> applyTransformations(classNode, annotationNode) }

    for (fieldNode in classNode.fields) {
      fieldNode.annotations.forEach { annotationNode -> applyTransformations(fieldNode, annotationNode) }
    }

    for (methodNode in classNode.methods) {
      methodNode.annotations.forEach { annotationNode -> applyTransformations(methodNode, annotationNode) }
    }

    for (innerClass in classNode.innerClasses) {
      applyTransformations(innerClass)
    }
  }

  private fun applyTransformations(node: AstNode, annotation: AnnotationNode) {
    map[annotation.type]?.forEach { transformation ->
      try {
        transformation.transform(node, annotation)
      } catch (e: Exception) {
        if (e is MarcelSemanticException) throw e
        System.err.println("Error while applying AST transformation ${transformation.javaClass} from annotation ${annotation.type}")
        e.printStackTrace()
      }
    }
  }

  private fun doLoadTransformations(semantic: MarcelSemantic, classNode: ClassCstNode) {
    val javaType = typeResolver.of(classNode.className) as NotLoadedJavaType
    loadFromAnnotations(semantic, ElementType.TYPE, javaType, classNode.annotations)
    classNode.fields.forEach { fieldNode ->
      loadFromAnnotations(semantic, ElementType.FIELD, javaType, fieldNode.annotations)
    }
    classNode.methods.forEach { methodNode ->
      loadFromAnnotations(semantic, ElementType.METHOD, javaType, methodNode.annotations)
    }
    classNode.innerClasses.forEach { doLoadTransformations(semantic, it) }
  }

  // also init and apply type transformation. TODO document that clearly, also for self
  private fun loadFromAnnotations(semantic: MarcelSemantic,
                                  elementType: ElementType,
                                  classType: NotLoadedJavaType, annotations: List<AnnotationCstNode>) {
    annotations.asSequence()
      .map { semantic.visit(it, elementType) }
      .filter { annotation -> annotation.type.isLoaded
          && annotation.type.realClazz.getAnnotation(MarcelAstTransformationClass::class.java) != null }
      .forEach { annotation ->
        val transformations = map.computeIfAbsent(annotation.type, this::getTransformations)
        if (transformations.isNotEmpty()) {
          transformations.forEach { transformation ->
            transformation.init(typeResolver)
            try {
              transformation.transformType(classType, annotation)
            } catch (e: Exception) {
              if (e is MarcelSemanticException) throw e
              System.err.println("Error while applying AST transformation ${transformation.javaClass} from annotation ${annotation.type}")
              e.printStackTrace()
            }
          }
        } else {
          map.remove(annotation.type)
        }
      }
  }

  // TODO log in case of errors in this function
  private fun getTransformations(annotationType: JavaAnnotationType): List<AstTransformation> {
    val transformationAnnotation = annotationType.realClazz.getAnnotation(MarcelAstTransformationClass::class.java)
    val classes =
      if (transformationAnnotation.classes.isNotEmpty()) transformationAnnotation.classes.map { it.java }
      else transformationAnnotation.value.mapNotNull {
        try {
          Class.forName(it)
        } catch (e: ClassNotFoundException) { null }
      }
        .toList()

    return classes.mapNotNull {
      try {
        it.getDeclaredConstructor().newInstance() as AstTransformation
      } catch (e: ReflectiveOperationException) {
        null
      }
    }
  }
}