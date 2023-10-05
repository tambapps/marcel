package com.tambapps.marcel.parser.cst

import com.tambapps.marcel.parser.cst.expression.ExpressionCstNodeVisitor
import com.tambapps.marcel.parser.cst.statement.StatementCstNodeVisitor

interface CstNodeVisitor<T>: ExpressionCstNodeVisitor<T>, StatementCstNodeVisitor<T> {


}