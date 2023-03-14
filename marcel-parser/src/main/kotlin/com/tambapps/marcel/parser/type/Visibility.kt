package com.tambapps.marcel.parser.type

import java.lang.reflect.Modifier

enum class Visibility {
    /**
     * Public visibility
     */
    PUBLIC,

    /**
     * Protected visibility
     */
    PROTECTED,

    /**
     * Package private
     */
    INTERNAL,

    /**
     * Private
     */
    PRIVATE;

    companion object {
        fun fromAccess(flags: Int): Visibility {
            return when {
                flags and Modifier.PRIVATE != 0 -> PRIVATE
                flags and Modifier.PROTECTED != 0 -> PROTECTED
                flags and Modifier.PUBLIC != 0 -> PUBLIC
                else -> INTERNAL
            }
        }
    }
}