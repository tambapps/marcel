package com.tambapps.marcel.lexer;
import static com.tambapps.marcel.lexer.TokenType.*;
import java.util.Stack;

/**
  * Marcel lang lexer
  */
%%
%class MarcelJflexer

%{

     private static final class State {
            final int lBraceCount;
            final int state;

            public State(int state, int lBraceCount) {
                this.state = state;
                this.lBraceCount = lBraceCount;
            }

            @Override
            public String toString() {
                return "yystate = " + state + (lBraceCount == 0 ? "" : "lBraceCount = " + lBraceCount);
            }
        }

    private final Stack<State> states = new Stack<State>();
    private int lBraceCount;

    private int commentStart;
    private int commentDepth;

    private void pushState(int state) {
        states.push(new State(yystate(), lBraceCount));
        lBraceCount = 0;
        yybegin(state);
    }

    private void popState() {
        State state = states.pop();
        lBraceCount = state.lBraceCount;
        yybegin(state.state);
    }

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

%xstate STRING RAW_STRING SHORT_TEMPLATE_ENTRY BLOCK_COMMENT DOC_COMMENT
%state LONG_TEMPLATE_ENTRY UNMATCHED_BACKTICK

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

CHARACTER_LITERAL="'"([^\\\'\n]|{ESCAPE_SEQUENCE})*("'"|\\)?

ESCAPE_SEQUENCE=\\(u{HEX_DIGIT}{HEX_DIGIT}{HEX_DIGIT}{HEX_DIGIT}|[^\n])

// ANY_ESCAPE_SEQUENCE = \\[^]
THREE_QUO = (\"\"\")
THREE_OR_MORE_QUO = ({THREE_QUO}\"*)

REGULAR_STRING_PART=[^\\\"\n\$]+
SHORT_TEMPLATE_ENTRY=\${IDENTIFIER}
LONELY_DOLLAR=\$
LONG_TEMPLATE_ENTRY_START=\$\{
LONELY_BACKTICK=`
%%

{INTEGER_LITERAL} { return valueToken(INTEGER); }

// String templates

{THREE_QUO}                      { pushState(RAW_STRING); return token(OPEN_QUOTE); }
<RAW_STRING> \n                  { return valueToken(REGULAR_STRING_PART); }
<RAW_STRING> \"                  { return valueToken(REGULAR_STRING_PART); }
<RAW_STRING> \\                  { return valueToken(REGULAR_STRING_PART); }
<RAW_STRING> {THREE_OR_MORE_QUO} {
                                    int length = yytext().length();
                                    if (length <= 3) { // closing """
                                        popState();
                                        return token(CLOSING_QUOTE);
                                    }
                                    else { // some quotes at the end of a string, e.g. """ "foo""""
                                        yypushback(3); // return the closing quotes (""") to the stream
                                        return valueToken(REGULAR_STRING_PART);
                                    }
                                 }

\"                          { pushState(STRING); return token(OPEN_QUOTE); }
<STRING> \n                 { popState(); yypushback(1); return valueToken(DANGLING_NEWLINE); }
<STRING> \"                 { popState(); return token(CLOSING_QUOTE); }
<STRING> {ESCAPE_SEQUENCE}  { return valueToken(ESCAPE_SEQUENCE); }

<STRING, RAW_STRING> {REGULAR_STRING_PART}         { return valueToken(REGULAR_STRING_PART); }
<STRING, RAW_STRING> {SHORT_TEMPLATE_ENTRY}        {
                                                        pushState(SHORT_TEMPLATE_ENTRY);
                                                        yypushback(yylength() - 1);
                                                        return token(SHORT_TEMPLATE_ENTRY_START);
                                                   }
// Only *this* keyword is itself an expression valid in this position
// *null*, *true* and *false* are also keywords and expression, but it does not make sense to put them
// in a string template for it'd be easier to just type them in without a dollar
<SHORT_TEMPLATE_ENTRY> {IDENTIFIER}    { popState(); return valueToken(IDENTIFIER); }

<STRING, RAW_STRING> {LONELY_DOLLAR}               { return valueToken(REGULAR_STRING_PART); }
<STRING, RAW_STRING> {LONG_TEMPLATE_ENTRY_START}   { pushState(LONG_TEMPLATE_ENTRY); return token(LONG_TEMPLATE_ENTRY_START); }

<LONG_TEMPLATE_ENTRY> "{"              { lBraceCount++; return valueToken(LBRACE); }
<LONG_TEMPLATE_ENTRY> "}"              {
                                           if (lBraceCount == 0) {
                                             popState();
                                             return token(LONG_TEMPLATE_ENTRY_END);
                                           }
                                           lBraceCount--;
                                           return valueToken(RBRACE);
                                       }


({WHITE_SPACE_CHAR})+ { return token(WHITE_SPACE); }

// keywords
"void"          { return token(TYPE_VOID); }
"true"          { return token(VALUE_TRUE); }
"false"          { return token(VALUE_FALSE); }
"byte"          { return token(TYPE_BYTE); }
"short"          { return token(TYPE_SHORT); }
"int"          { return token(TYPE_INT); }
"long"          { return token(TYPE_LONG); }
"float"          { return token(TYPE_FLOAT); }
"double"          { return token(TYPE_DOUBLE); }
"bool"          { return token(TYPE_BOOL); }
"public"          { return token(VISIBILITY_PUBLIC); }
"protected"          { return token(VISIBILITY_PROTECTED); }
"hidden"          { return token(VISIBILITY_HIDDEN); }
"private"          { return token(VISIBILITY_PRIVATE); }
"fun"          { return token(FUN); }
"return"          { return token(RETURN); }
"import"          { return token(IMPORT); }
"as"          { return token(AS); }
"new"          { return token(NEW); }

{IDENTIFIER} {  return valueToken(IDENTIFIER); }

// symbols
"("          { return token(LPAR); }
")"          { return token(RPAR); }
"{"          { return token(BRACKETS_OPEN); }
"}"          { return token(BRACKETS_CLOSE); }
":"          { return token(COLON); }
";"          { return token(SEMI_COLON); }
"+"          { return token(PLUS); }
"-"          { return token(MINUS); }
"/"          { return token(DIV); }
"*"          { return token(MUL); }
"%"          { return token(MODULO); }
"!"          { return token(NOT); }
"&&"          { return token(AND); }
"||"          { return token(OR); }
">="          { return token(GOE); }
">"          { return token(GT); }
"<="          { return token(LOE); }
"<"          { return token(LT); }
"=="          { return token(EQUAL); }
"!="          { return token(NOT_EQUAL); }
"="          { return token(ASSIGNMENT); }
"."          { return token(DOT); }