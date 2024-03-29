package com.tambapps.marcel.lexer;

// need to keep it Java as it is used by MarcelJflexer
/**
 * Token Type
 */
public enum TokenType {
  IDENTIFIER,
  WHITE_SPACE,
  // keywords
  TYPE_INT, TYPE_LONG, TYPE_SHORT, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOL, TYPE_BYTE, TYPE_VOID, TYPE_CHAR, FUN, RETURN,
  VALUE_TRUE, VALUE_FALSE, NEW, IMPORT, AS, INLINE, STATIC, FOR, IN, IF, ELSE, NULL, BREAK, CONTINUE, DEF, DO,
  CLASS, EXTENSION, PACKAGE, EXTENDS, IMPLEMENTS, FINAL, SWITCH, WHEN, THIS, SUPER, DUMBBELL, TRY, CATCH, FINALLY,
  INSTANCEOF, NOT_INSTANCEOF, THROW, THROWS, CONSTRUCTOR, DYNOBJ, ASYNC,
  // visibilities
  VISIBILITY_PUBLIC, VISIBILITY_PROTECTED, VISIBILITY_INTERNAL, VISIBILITY_PRIVATE,

// type constants
  INTEGER, FLOAT,


  BLOCK_COMMENT, DOC_COMMENT, HASH, SHEBANG_COMMENT, EOL_COMMENT,


  // symbols
  // operators
  PLUS, MINUS, DIV, MUL, COMMA, QUESTION_MARK, INCR, DECR, FIND, ELVIS,
  PLUS_ASSIGNMENT, MINUS_ASSIGNMENT, MUL_ASSIGNMENT, DIV_ASSIGNMENT, MODULO_ASSIGNMENT, TWO_DOTS, TWO_DOTS_END_EXCLUSIVE, QUESTION_DOT,
  WHILE, POWER, NOT, MODULO, AND, EQUAL, NOT_EQUAL, GT, LT, GOE, LOE, OR, ASSIGNMENT,
  IS, IS_NOT, LEFT_SHIFT, RIGHT_SHIFT, // IS and IS_NOT are === and !==


  ARROW, BRACKETS_OPEN, BRACKETS_CLOSE, SQUARE_BRACKETS_OPEN, QUESTION_SQUARE_BRACKETS_OPEN, SQUARE_BRACKETS_CLOSE, LPAR, RPAR, COLON, SEMI_COLON, PIPE,
  OPEN_QUOTE, CLOSING_QUOTE, REGULAR_STRING_PART,
  OPEN_CHAR_QUOTE, CLOSING_CHAR_QUOTE,
  OPEN_REGEX_QUOTE, CLOSING_REGEX_QUOTE,
  OPEN_SIMPLE_QUOTE, CLOSING_SIMPLE_QUOTE,
  DANGLING_NEWLINE, ESCAPE_SEQUENCE, LBRACE, RBRACE, DOT, AT,

  // shortTemplateEntry: $
  // LONG_TEMPLATE_ENTRY_START: ${}
  SHORT_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_END,


  END_OF_FILE, BAD_CHARACTER
}
