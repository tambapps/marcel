package com.tambapps.marcel.semantic.type

/**
 * Object representing any given [JavaType]. Can be useful when checking paths with exception throwing,
 * as we could replace the type of sych path with Anything
 */
object AnythingJavaType: LoadedObjectType(Object::class.java)