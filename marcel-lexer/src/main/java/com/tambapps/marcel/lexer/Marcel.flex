package com.tambapps.marcel.lexer;
import static com.tambapps.marcel.lexer.TokenType.*;

/**
  * Marcel lang lexer
  */
%%
%class MarcelJflexer

%{
  // tokens for which we need to save current buffer
  private LexToken valueToken(TokenType tokenType) {
    return new LexToken(tokenType, new String(zzBuffer, zzCurrentPos, zzMarkedPos - zzCurrentPos));
  }
  private LexToken token(TokenType tokenType) {
    return new LexToken(tokenType, null);
  }

%}

%unicode
%line
%column

/** spec of the function lexing **/
// TODO find way of including line and column when throwing exception
%scanerror MarcelLexerException
%function nextToken
%type LexToken
%eof{
  // end of file
%eof}
LETTER = [:letter:]|_
IDENTIFIER_PART=[:digit:]|{LETTER}

IDENTIFIER = {PLAIN_IDENTIFIER}|{ESCAPED_IDENTIFIER}
PLAIN_IDENTIFIER={LETTER} {IDENTIFIER_PART}*
ESCAPED_IDENTIFIER = `[^`\n]+`

/** Numbers **/
DIGIT=[0-9]
DIGIT_OR_UNDERSCORE = [_0-9]
DIGITS = {DIGIT} {DIGIT_OR_UNDERSCORE}*
HEX_DIGIT=[0-9A-Fa-f]
HEX_DIGIT_OR_UNDERSCORE = [_0-9A-Fa-f]
WHITE_SPACE_CHAR=[\ \n\t\f]

INTEGER_LITERAL={DECIMAL_INTEGER_LITERAL}|{HEX_INTEGER_LITERAL}|{BIN_INTEGER_LITERAL}
DECIMAL_INTEGER_LITERAL=(0|([1-9]({DIGIT_OR_UNDERSCORE})*)){TYPED_INTEGER_SUFFIX}
HEX_INTEGER_LITERAL=0[Xx]({HEX_DIGIT_OR_UNDERSCORE})*{TYPED_INTEGER_SUFFIX}
BIN_INTEGER_LITERAL=0[Bb]({DIGIT_OR_UNDERSCORE})*{TYPED_INTEGER_SUFFIX}
LONG_SUFFIX=[Ll]
UNSIGNED_SUFFIX=[Uu]
TYPED_INTEGER_SUFFIX = {UNSIGNED_SUFFIX}?{LONG_SUFFIX}?
%%

{INTEGER_LITERAL} { return valueToken(INTEGER); }

{IDENTIFIER} {  return valueToken(IDENTIFIER); }

"("          { return token(LPAR); }
")"          { return token(RPAR); }
":"          { return token(COLON); }
";"          { return token(SEMI_COLON); }