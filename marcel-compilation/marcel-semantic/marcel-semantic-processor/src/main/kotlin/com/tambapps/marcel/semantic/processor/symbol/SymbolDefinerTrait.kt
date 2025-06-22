package com.tambapps.marcel.semantic.processor.symbol

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.cst.ClassCstNode
import com.tambapps.marcel.parser.cst.EnumCstNode
import com.tambapps.marcel.parser.cst.ScriptCstNode
import com.tambapps.marcel.semantic.processor.MarcelSemantic
import com.tambapps.marcel.semantic.Visibility
import com.tambapps.marcel.semantic.exception.MarcelSemanticException
import com.tambapps.marcel.semantic.exception.MemberNotVisibleException
import com.tambapps.marcel.semantic.extensions.javaType
import com.tambapps.marcel.semantic.symbol.type.JavaType
import com.tambapps.marcel.semantic.symbol.type.SourceJavaType
import java.lang.Enum

/**
 * Define symbols (classes, methods, fields) of multiple marcel semantics in a lazy way to avoid failing
 * to do so when a type is referenced from another semantic CST
 */
interface SymbolDefinerTrait {

  /**
   * Define a new type
   *
   * @param token the token to use referencing the location in case of error
   * @param javaType the type to define
   */
  fun defineType(token: LexToken = LexToken.DUMMY, javaType: JavaType)

  /**
   * Define classes, methods, fields of all CSTs
   *
   * @param semantics all the semantic CSTs
   * @param scriptParentType the script parent type to use when encountering a script node
   */
  fun defineSymbols(semantics: List<MarcelSemantic>, scriptParentType: JavaType) {
    val triples = defineTypes(semantics, scriptParentType)
    for ((semantic, classCstNode, classType) in triples) {
      semantic.defineClassMembers(
        classCstNode, classType,
        // not recursive because we're iterating over ALL class cst nodes, even inner ones
        recursive = false
      )
    }
  }

  // predefining types, but not fully to avoid trying to find types we haven't predefined yet
  private fun defineTypes(
    semantics: List<MarcelSemantic>,
    scriptParentType: JavaType
  ): MutableList<Triple<MarcelSemantic, ClassCstNode, SourceJavaType>> {
    // first define types, without super parent because one supertype may reference a type from another class that wasn't defined yet
    val toDefineTypes = mutableListOf<Triple<MarcelSemantic, ClassCstNode, SourceJavaType>>()
    for (s in semantics) {
      s.cst.classes.forEach { predefineTypes(s, it, toDefineTypes) }
    }

    // then make the semantic resolve imports, now that we registered the existence of all source types
    for (s in semantics) {
      s.resolveImports()
    }

    // define scripts after the rest, because script might use other classes, but it can't be the other way around
    for (s in semantics) {
      s.cst.script?.let { predefineTypes(s, it, toDefineTypes) }
    }
    // now that we defined everything we can complete the definition of all types

    for ((semantic, classCstNode, classType) in toDefineTypes) {
      val superType =
        if (classCstNode is ScriptCstNode) scriptParentType
        else classCstNode.superType?.let { semantic.resolve(it) } ?: JavaType.Object
      if (!superType.isVisibleFrom(classType)) {
        throw MemberNotVisibleException(classCstNode, superType, classType)
      }
      if (superType.isFinal) {
        throw MarcelSemanticException(classCstNode, "Class $superType is final and therefore cannot be extended")
      }
      if (superType.isInterface) {
        throw MarcelSemanticException(classCstNode, "Cannot extend an interface")
      }
      if (!classType.isEnum) { // we don't want to override the Enum super class given in the pre-definition
        classType.superType = superType
      }
      classCstNode.forExtensionType?.let { classType.globalExtendedType = semantic.resolve(it) }
      classType.directlyImplementedInterfaces.addAll(classCstNode.interfaces.map { semantic.resolve(it) })
      for (interfaceType in classType.directlyImplementedInterfaces) {
        if (!interfaceType.isInterface) {
          throw MarcelSemanticException(classCstNode, "Cannot implement a non-interface")
        }
        if (!interfaceType.isVisibleFrom(classType)) {
          throw MemberNotVisibleException(classCstNode, interfaceType, classType)
        }
      }
    }
    return toDefineTypes
  }

  /**
   * Define java types without taking care of associating their parent types and implemented interface
   */
  private fun predefineTypes(
    s: MarcelSemantic,
    classNode: ClassCstNode,
    toDefineTypes: MutableList<Triple<MarcelSemantic, ClassCstNode, SourceJavaType>>
  ) {
    val isEnum = classNode is EnumCstNode
    val classType = SourceJavaType(
      visibility = Visibility.fromTokenType(classNode.access.visibility),
      className = classNode.className,
      genericTypes = emptyList(),
      superType = null, // will be set later
      globalExtendedType = null, // will be set later
      isInterface = classNode.isInterface,
      isFunctionalInterface = classNode.isInterface && classNode.methods.count { it.isAbstract } == 1,
      directlyImplementedInterfaces = mutableSetOf(),
      isScript = classNode.isScript,
      isEnum = isEnum,
      isExtensionType = classNode.isExtensionClass,
      isFinal = classNode.access.isFinal || classNode.isEnum,
      isAnnotation = false, // not supported
      isAbstract = false // not supported
    )
    if (isEnum) {
      classType.superType = Enum::class.javaType.withGenericTypes(classType)

      // TODO define static fields and static methods that enum class provides
    }
    defineType(classNode.token, classType)
    toDefineTypes.add(Triple(s, classNode, classType))
    classNode.innerClasses.forEach { predefineTypes(s, it, toDefineTypes) }
  }

}