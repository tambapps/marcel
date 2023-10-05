package com.tambapps.marcel.parser.cst.expression

import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor

interface CstNodeVisitor<T>: ExpressionCstNodeVisitor<T>, StatementCstNodeVisitor<T> {

}