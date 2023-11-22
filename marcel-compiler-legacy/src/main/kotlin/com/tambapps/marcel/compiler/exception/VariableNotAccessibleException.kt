package com.tambapps.marcel.compiler.exception

import com.tambapps.marcel.lexer.LexToken
import com.tambapps.marcel.parser.exception.MarcelSemanticLegacyException
import com.tambapps.marcel.parser.scope.JavaField
import com.tambapps.marcel.parser.scope.Variable
import com.tambapps.marcel.parser.type.JavaType

class VariableNotAccessibleException(token: LexToken, variable: Variable, from: JavaType)
    : MarcelSemanticLegacyException(token,
        if (variable is JavaField)
            "Field ${variable.name} from ${variable.owner.className} isn't accessible from class ${from.className}"
        else "Variable ${variable.name} isn't accessible from class ${from.className}"
        ) {
}