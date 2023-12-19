package com.tambapps.marcel.semantic.type

import com.tambapps.marcel.parser.cst.ClassNode
import com.tambapps.marcel.parser.cst.ScriptNode
import com.tambapps.marcel.semantic.MarcelSemantic
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.extensions.javaType
import marcel.lang.Script

/**
 * Define symbols of multiple marcel semantics in a lazy way to avoid failing to do so when a type is referenced from
 * another semantic
 */
class SymbolsDefiner(
  private val typeResolver: JavaTypeResolver
) {

  fun defineSymbols(semantics: List<MarcelSemantic>) {
    val triples = defineTypes(semantics)
    for ((semantic, classCstNode, classType) in triples) {
      semantic.defineClassMembers(classCstNode, classType,
        // not recursive because we're iterating over ALL class cst nodes, even inner ones
        recursive = false)
    }
  }

  // predefining types, but not fully to avoid trying to find types we haven't predefined yet
  private fun defineTypes(semantics: List<MarcelSemantic>): MutableList<Triple<MarcelSemantic, ClassNode, NotLoadedJavaType>> {
    // first define types, without super parent because one supertype may reference a type from another class that wasn't defined yet
    val toDefineTypes = mutableListOf<Triple<MarcelSemantic, ClassNode, NotLoadedJavaType>>()
    for (s in semantics) {
      s.cst.classes.forEach { predefineTypes(s, it, toDefineTypes) }
    }

    // define scripts after the rest, because script might use other classes but it can't be the other way around
    for (s in semantics) {
      s.cst.script?.let { predefineTypes(s, it, toDefineTypes) }
    }
    // now that we defined everything we can complete the definition of all types

    for ((semantic, classCstNode, classType) in toDefineTypes) {
      val superType =
        if (classCstNode is ScriptNode) Script::class.javaType
        else classCstNode.superType?.let { semantic.visit(it) } ?: JavaType.Object
      if (!superType.isAccessibleFrom(classType)) {
        throw MarcelSemanticException(classCstNode, "Class $superType is not accessible from $classType")
      }
      if (superType.isFinal) {
        throw MarcelSemanticException(classCstNode, "Class $superType is final and therefore cannot be extended")
      }
      if (superType.isInterface) {
        throw MarcelSemanticException(classCstNode, "Cannot extend an interface")
      }
      classType.superType = superType
      classType.directlyImplementedInterfaces.addAll(classCstNode.interfaces.map { semantic.visit(it) })
      for (interfaceType in classType.directlyImplementedInterfaces) {
        if (!interfaceType.isInterface) {
          throw MarcelSemanticException(classCstNode, "Cannot implement a non-interface")
        }
        if (!interfaceType.isAccessibleFrom(classType)) {
          throw MarcelSemanticException(classCstNode, "Class $interfaceType is not accessible from $classType")
        }
      }
    }
    return toDefineTypes
  }

  /**
   * Define java types without taking care of associating their parent types and implemented interface
   */
  private fun predefineTypes(s: MarcelSemantic,
                             classNode: ClassNode,
                             toDefineTypes: MutableList<Triple<MarcelSemantic, ClassNode, NotLoadedJavaType>>) {
    val classType = NotLoadedJavaType(
      visibility = Visibility.fromTokenType(classNode.access.visibility),
      className = classNode.className,
      genericTypes = emptyList(),
      superType = null, // will be set later
      isInterface = false, directlyImplementedInterfaces = mutableSetOf(), isScript = classNode is ScriptNode)
    typeResolver.defineType(classNode.token, classType)
    toDefineTypes.add(Triple(s, classNode, classType))
    classNode.innerClasses.forEach { predefineTypes(s, it, toDefineTypes) }
  }

}