package com.tambapps.marcel.lexer;

public enum TokenType {
  IDENTIFIER,
  WHITE_SPACE,
  // keywords
  TYPE_INT, TYPE_LONG, TYPE_SHORT, TYPE_FLOAT, TYPE_DOUBLE, TYPE_BOOL, TYPE_BYTE, TYPE_VOID, FUN, RETURN,
  VALUE_TRUE, VALUE_FALSE, NEW, IMPORT, AS, STATIC,


// type constants
  INTEGER,


  // symbols
  PLUS, MINUS, DIV, MUL, COMMA, QUESTION_MARK, LPAR, RPAR, COLON, SEMI_COLON,
  POWER, NOT, MODULO, AND, EQUAL, NOT_EQUAL, GT, LT, GOE, LOE, OR, ASSIGNMENT, BRACKETS_OPEN, BRACKETS_CLOSE,
  OPEN_QUOTE, REGULAR_STRING_PART, CLOSING_QUOTE, DANGLING_NEWLINE, ESCAPE_SEQUENCE, LBRACE, RBRACE, DOT,
  // shortTemplateEntry: $
  // LONG_TEMPLATE_ENTRY_START: ${}
  SHORT_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_START, LONG_TEMPLATE_ENTRY_END,


  // visibilities
  VISIBILITY_PUBLIC, VISIBILITY_PROTECTED, VISIBILITY_INTERNAL, VISIBILITY_PRIVATE,


  END_OF_FILE
}
