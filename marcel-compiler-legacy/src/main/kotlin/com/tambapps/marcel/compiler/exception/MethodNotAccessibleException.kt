package com.tambapps.marcel.compiler.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.exception.MarcelSemanticException
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

class MethodNotAccessibleException(token: LexToken, method: JavaMethod, from: JavaType)
    : MarcelSemanticException(token, "Method ${method.name} from ${method.ownerClass.className} isn't accessible from class ${from.className}") {
}