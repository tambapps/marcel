package com.tambapps.marcel.lexer;

public enum TokenType {
  IDENTIFIER,
  WHITE_SPACE,
  // keywords
  TYPE_INT, TYPE_LONG, TYPE_SHORT, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOL, TYPE_BYTE, TYPE_VOID, TYPE_CHAR, FUN, RETURN,
  VALUE_TRUE, VALUE_FALSE, NEW, IMPORT, AS, INLINE, STATIC, FOR, IN, IF, ELSE, NULL, BREAK, CONTINUE, DEF,
  CLASS, EXTENSION, PACKAGE, EXTENDS, IMPLEMENTS, FINAL, SWITCH,


// type constants
  INTEGER, FLOAT,


  BLOCK_COMMENT, DOC_COMMENT, HASH, SHEBANG_COMMENT, EOL_COMMENT,


  // symbols
  PLUS, MINUS, DIV, MUL, COMMA, QUESTION_MARK, LPAR, RPAR, COLON, SEMI_COLON, INCR, DECR, ARROW,
  PLUS_ASSIGNMENT, MINUS_ASSIGNMENT, MUL_ASSIGNMENT, DIV_ASSIGNMENT, TWO_DOTS, QUESTION_DOT,
  WHILE, POWER, NOT, MODULO, AND, EQUAL, NOT_EQUAL, GT, LT, GOE, LOE, OR, ASSIGNMENT, BRACKETS_OPEN, BRACKETS_CLOSE,
  OPEN_QUOTE, OPEN_CHAR_QUOTE, REGULAR_STRING_PART, CLOSING_QUOTE, CLOSING_CHAR_QUOTE, DANGLING_NEWLINE, ESCAPE_SEQUENCE, LBRACE, RBRACE, DOT, LEFT_SHIFT,
  RIGHT_SHIFT, SQUARE_BRACKETS_OPEN, SQUARE_BRACKETS_CLOSE,
  // shortTemplateEntry: $
  // LONG_TEMPLATE_ENTRY_START: ${}
  SHORT_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_END,


  // visibilities
  VISIBILITY_PUBLIC, VISIBILITY_PROTECTED, VISIBILITY_INTERNAL, VISIBILITY_PRIVATE,


  END_OF_FILE
}