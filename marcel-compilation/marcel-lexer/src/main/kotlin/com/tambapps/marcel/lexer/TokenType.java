package com.tambapps.marcel.lexer;

// need to keep it Java as it is used by MarcelJflexer
/**
 * Token Type
 */
public enum TokenType {
  IDENTIFIER,
  WHITESPACE, LINE_RETURN,
  // keywords
  TYPE_INT, TYPE_LONG, TYPE_SHORT, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOL, TYPE_BYTE, TYPE_VOID, TYPE_CHAR, FUN, RETURN,
  VALUE_TRUE, VALUE_FALSE, NEW, IMPORT, AS, INLINE, STATIC, FOR, IN, IF, ELSE, NULL, BREAK, CONTINUE, DEF, DO,
  CLASS, EXTENSION, PACKAGE, EXTENDS, IMPLEMENTS, FINAL, SWITCH, WHEN, NOT_WHEN("!when"), THIS, SUPER, DUMBBELL, TRY, CATCH, FINALLY, WHILE,
  INSTANCEOF, NOT_INSTANCEOF("!instanceof"), THROW, THROWS, CONSTRUCTOR, DYNOBJ, ASYNC, ENUM, OVERRIDE,
  // visibilities
  VISIBILITY_PUBLIC, VISIBILITY_PROTECTED, VISIBILITY_INTERNAL, VISIBILITY_PRIVATE,

  // type constants
  INTEGER, FLOAT,


  BLOCK_COMMENT, DOC_COMMENT, HASH, SHEBANG_COMMENT, EOL_COMMENT,


  // symbols
  // operators
  PLUS("+"), MINUS("-"), DIV("/"), MUL("*"), COMMA(","), QUESTION_MARK("?"), INCR("++"), DECR("--"), FIND("=~"), ELVIS("?:"),
  PLUS_ASSIGNMENT("+="), MINUS_ASSIGNMENT("-="), MUL_ASSIGNMENT("*="), DIV_ASSIGNMENT("/="), MODULO_ASSIGNMENT("%="), TWO_DOTS(".."), TWO_DOTS_END_EXCLUSIVE("..<"), QUESTION_DOT("?."), THREE_DOTS("..."),
  POWER("**"), NOT("!"), NON_NULL("!!"), MODULO("%"), AND("&&"), EQUAL("=="), NOT_EQUAL("!="), GT(">"), LT("<"), GOE(">="), LOE("<="), OR("||"), ASSIGNMENT("="),
  IS("==="), IS_NOT("!=="), LEFT_SHIFT("<<"), RIGHT_SHIFT(">>"), // IS and IS_NOT are === and !==


  ARROW("->"), AND_ARROW("&>"), OR_ARROW("|>"), BRACKETS_OPEN("{"), BRACKETS_CLOSE("}"), SQUARE_BRACKETS_OPEN("["), QUESTION_SQUARE_BRACKETS_OPEN("?["), SQUARE_BRACKETS_CLOSE("]"),
  LPAR("("), RPAR(")"), COLON(":"), SEMI_COLON(";"), PIPE("|"),
  OPEN_QUOTE("\""), CLOSING_QUOTE("\""), REGULAR_STRING_PART("regular string"),
  OPEN_CHAR_QUOTE("`"), CLOSING_CHAR_QUOTE("`"),
  OPEN_REGEX_QUOTE("r/"), CLOSING_REGEX_QUOTE("/"),
  OPEN_SIMPLE_QUOTE("'"), CLOSING_SIMPLE_QUOTE("'"),
  DANGLING_NEWLINE, ESCAPE_SEQUENCE("\\"), LBRACE("{"), RBRACE("}"), DOT("."), AT("@"),


  SHORT_TEMPLATE_ENTRY_START("$"), LONG_TEMPLATE_ENTRY_START("${"), LONG_TEMPLATE_ENTRY_END("}"),


  END_OF_FILE("end of file"), BAD_CHARACTER;

  TokenType() {
    this(null);
  }
  TokenType(String value) {
    this.value = value != null ? value : name().toLowerCase().replace('_', ' ');
  }

  private final String value;

  @Override
  public String toString() {
    return value;
  }
}
