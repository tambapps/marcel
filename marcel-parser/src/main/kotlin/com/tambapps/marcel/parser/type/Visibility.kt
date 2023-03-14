package com.tambapps.marcel.parser.type

import java.lang.reflect.Modifier

enum class Visibility {
    /**
     * Public visibility
     */
    PUBLIC {
        override fun canAccess(from: JavaType, memberOwner: JavaType) = true
    },

    /**
     * Protected visibility
     */
    PROTECTED {
        override fun canAccess(from: JavaType, memberOwner: JavaType) =
            memberOwner.packageName == memberOwner.packageName || memberOwner.isAssignableFrom(from)
    },

    /**
     * Package private
     */
    INTERNAL {
        override fun canAccess(from: JavaType, memberOwner: JavaType) =
            memberOwner.packageName == from.packageName
    },

    /**
     * Private
     */
    PRIVATE {
        override fun canAccess(from: JavaType, memberOwner: JavaType) = from == memberOwner
    };

    abstract fun canAccess(from: JavaType, memberOwner: JavaType): Boolean

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