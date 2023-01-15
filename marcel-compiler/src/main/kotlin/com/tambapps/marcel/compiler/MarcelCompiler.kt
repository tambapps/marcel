package com.tambapps.marcel.compiler

import com.tambapps.marcel.compiler.bytecode.BytecodeWriter
import com.tambapps.marcel.lexer.MarcelLexer
import com.tambapps.marcel.lexer.MarcelLexerException
import com.tambapps.marcel.parser.MarcelParser
import com.tambapps.marcel.parser.MarcelParsingException
import com.tambapps.marcel.parser.exception.SemanticException
import java.io.IOException
import java.io.Reader

class MarcelCompiler {

  @Throws( IOException::class, MarcelLexerException::class, MarcelParsingException::class, SemanticException::class)
  fun compile(reader: Reader, className: String? = null): CompilationResult {
    val tokens = MarcelLexer().lex(reader)
    val parser = if (className != null) MarcelParser(className, tokens) else MarcelParser(tokens)
    val ast = parser.parse()
    return BytecodeWriter().generate(ast)
  }

}