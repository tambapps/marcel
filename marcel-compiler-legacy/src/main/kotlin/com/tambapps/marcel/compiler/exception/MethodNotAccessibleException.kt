package com.tambapps.marcel.compiler.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.exception.MarcelSemanticLegacyException
import com.tambapps.marcel.parser.type.JavaMethod
import com.tambapps.marcel.parser.type.JavaType

class MethodNotAccessibleException(token: LexToken, method: JavaMethod, from: JavaType)
    : MarcelSemanticLegacyException(token, "Method ${method.name} from ${method.ownerClass.className} isn't accessible from class ${from.className}") {
}