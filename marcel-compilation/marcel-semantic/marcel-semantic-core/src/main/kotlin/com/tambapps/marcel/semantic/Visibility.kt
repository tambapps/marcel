package com.tambapps.marcel.semantic

import com.tambapps.marcel.lexer.TokenType
import com.tambapps.marcel.semantic.type.JavaType
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
            from.packageName == memberOwner.packageName || memberOwner.isAssignableFrom(from)
    },

    /**
     * Package private
     */
    INTERNAL {
        override fun canAccess(from: JavaType, memberOwner: JavaType) =
            from.packageName == memberOwner.packageName
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

        fun fromTokenType(type: TokenType) = when (type) {
            TokenType.VISIBILITY_PUBLIC -> PUBLIC
            TokenType.VISIBILITY_PROTECTED -> PROTECTED
            TokenType.VISIBILITY_INTERNAL -> INTERNAL
            TokenType.VISIBILITY_PRIVATE -> PRIVATE
            else -> throw IllegalArgumentException()
        }
    }
}