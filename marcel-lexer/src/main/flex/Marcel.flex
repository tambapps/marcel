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

        private LexToken commentStateToTokenType(int state) {
            switch (state) {
                case BLOCK_COMMENT:
                    return new LexToken(TokenType.BLOCK_COMMENT);
                case DOC_COMMENT:
                    return new LexToken(TokenType.DOC_COMMENT);
                default:
                    throw new IllegalArgumentException("Unexpected state: " + state);
            }
        }

  // tokens for which we need to save current buffer
  private LexToken valueToken(TokenType tokenType) {
    return new LexToken(tokenType, getTokenString());
  }
  private LexToken token(TokenType tokenType) {
    return new LexToken(tokenType, null);
  }

  public String getTokenString() {
    return new String(zzBuffer, getTokenStart(), getTokenEnd() - getTokenStart());
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }
%}

%unicode
%line
%column

/** spec of the function lexing **/
%scanerror MarcelLexerException
%function nextToken
%type LexToken
%eof{
  // end of file
%eof}

%xstate STRING RAW_STRING SHORT_TEMPLATE_ENTRY BLOCK_COMMENT DOC_COMMENT, CHAR_STRING
%state LONG_TEMPLATE_ENTRY UNMATCHED_BACKTICK

LETTER = [:letter:]|_
IDENTIFIER_PART=[:digit:]|{LETTER}|\$ // dollar for inner classes

IDENTIFIER = {PLAIN_IDENTIFIER}
PLAIN_IDENTIFIER={LETTER} {IDENTIFIER_PART}*

/** Numbers **/
DIGIT=[0-9]
DIGIT_OR_UNDERSCORE = [_0-9]
DIGITS = {DIGIT} {DIGIT_OR_UNDERSCORE}*
HEX_DIGIT=[0-9A-Fa-f]
HEX_DIGIT_OR_UNDERSCORE = [_0-9A-Fa-f]
WHITE_SPACE_CHAR=[\ \n\t\f]

EOL_COMMENT="/""/"[^\n]*
SHEBANG_COMMENT="#!"[^\n]*

INTEGER_LITERAL={DECIMAL_INTEGER_LITERAL}|{HEX_INTEGER_LITERAL}|{BIN_INTEGER_LITERAL}
DECIMAL_INTEGER_LITERAL=(0|([1-9]({DIGIT_OR_UNDERSCORE})*)){TYPED_INTEGER_SUFFIX}
HEX_INTEGER_LITERAL=0[Xx]({HEX_DIGIT_OR_UNDERSCORE})*{TYPED_INTEGER_SUFFIX}
BIN_INTEGER_LITERAL=0[Bb]({DIGIT_OR_UNDERSCORE})*{TYPED_INTEGER_SUFFIX}
LONG_SUFFIX=[Ll]
UNSIGNED_SUFFIX=[Uu]
TYPED_INTEGER_SUFFIX = {UNSIGNED_SUFFIX}?{LONG_SUFFIX}?

DOUBLE_LITERAL={FLOATING_POINT_LITERAL1}|{FLOATING_POINT_LITERAL2}|{FLOATING_POINT_LITERAL3}|{FLOATING_POINT_LITERAL4}|{DOUBLE_POINT_LITERAL1}|{DOUBLE_POINT_LITERAL2}|{DOUBLE_POINT_LITERAL3}|{DOUBLE_POINT_LITERAL4}
FLOATING_POINT_LITERAL1=({DIGITS})"."({DIGITS})+({EXPONENT_PART})?({FLOATING_POINT_LITERAL_SUFFIX})?
FLOATING_POINT_LITERAL2="."({DIGITS})({EXPONENT_PART})?({FLOATING_POINT_LITERAL_SUFFIX})?
FLOATING_POINT_LITERAL3=({DIGITS})({EXPONENT_PART})({FLOATING_POINT_LITERAL_SUFFIX})?
FLOATING_POINT_LITERAL4=({DIGITS})({FLOATING_POINT_LITERAL_SUFFIX})
DOUBLE_POINT_LITERAL1=({DIGITS})"."({DIGITS})+({EXPONENT_PART})?({DOUBLE_LITERAL_SUFFIX})?
DOUBLE_POINT_LITERAL2="."({DIGITS})({EXPONENT_PART})?({DOUBLE_LITERAL_SUFFIX})?
DOUBLE_POINT_LITERAL3=({DIGITS})({EXPONENT_PART})({DOUBLE_LITERAL_SUFFIX})?
DOUBLE_POINT_LITERAL4=({DIGITS})({DOUBLE_LITERAL_SUFFIX})
FLOATING_POINT_LITERAL_SUFFIX=[Ff]
DOUBLE_LITERAL_SUFFIX=[Dd]

EXPONENT_PART=[Ee]["+""-"]?({DIGIT_OR_UNDERSCORE})*
CHARACTER_LITERAL="'"([^\\\'\n]|{ESCAPE_SEQUENCE})*("'"|\\)?

ESCAPE_SEQUENCE=\\(u{HEX_DIGIT}{HEX_DIGIT}{HEX_DIGIT}{HEX_DIGIT}|[^\n])

// ANY_ESCAPE_SEQUENCE = \\[^]
THREE_QUO = (\"\"\")
THREE_OR_MORE_QUO = ({THREE_QUO}\"*)

REGULAR_STRING_PART=[^\\\"\n\$]+
REGULAR_SIMPLE_CHAR_STRING_PART=[^\\`]+
SHORT_TEMPLATE_ENTRY=\${IDENTIFIER}
LONELY_DOLLAR=\$
LONG_TEMPLATE_ENTRY_START=\$\{
LONELY_BACKTICK=`
%%

// actually int or long
{INTEGER_LITERAL} { return valueToken(INTEGER); }
{DOUBLE_LITERAL}     { return valueToken(FLOAT); }

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

\`                          { pushState(CHAR_STRING); return token(OPEN_CHAR_QUOTE); }
\"                          { pushState(STRING); return token(OPEN_QUOTE); }
<STRING> \n                 { popState(); yypushback(1); return valueToken(DANGLING_NEWLINE); }
<STRING> \"                 { popState(); return token(CLOSING_QUOTE); }
<CHAR_STRING> \`                 { popState(); return token(CLOSING_CHAR_QUOTE); }
<STRING> {ESCAPE_SEQUENCE}  { return valueToken(ESCAPE_SEQUENCE); }
<CHAR_STRING> {ESCAPE_SEQUENCE}  { return valueToken(ESCAPE_SEQUENCE); }

<STRING, RAW_STRING> {REGULAR_STRING_PART}         { return valueToken(REGULAR_STRING_PART); }
<CHAR_STRING> {REGULAR_SIMPLE_CHAR_STRING_PART}         { return valueToken(REGULAR_STRING_PART); }
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

// (Nested) comments

"/**/" {
    return token(TokenType.BLOCK_COMMENT);
}

"/**" {
    pushState(DOC_COMMENT);
    commentDepth = 0;
    commentStart = getTokenStart();
}

"/*" {
    pushState(BLOCK_COMMENT);
    commentDepth = 0;
    commentStart = getTokenStart();
}

<BLOCK_COMMENT, DOC_COMMENT> {
    "/*" {
         commentDepth++;
    }

    <<EOF>> {
        int state = yystate();
        popState();
        zzStartRead = commentStart;
        return commentStateToTokenType(state);
    }

    "*/" {
        if (commentDepth > 0) {
            commentDepth--;
        }
        else {
             int state = yystate();
             popState();
             zzStartRead = commentStart;
             return commentStateToTokenType(state);
        }
    }

    [\s\S] {}
}

({WHITE_SPACE_CHAR})+ { return token(WHITE_SPACE); }

{EOL_COMMENT} { return token(TokenType.EOL_COMMENT); }
{SHEBANG_COMMENT} {
            if (zzCurrentPos == 0) {
                return token(TokenType.SHEBANG_COMMENT);
            }
            else {
                yypushback(yylength() - 1);
                return token(HASH);
            }
          }

// keywords
"void"          { return valueToken(TYPE_VOID); }
"true"          { return token(VALUE_TRUE); }
"false"          { return token(VALUE_FALSE); }
"byte"          { return valueToken(TYPE_BYTE); }
"short"          { return valueToken(TYPE_SHORT); }
"int"          { return valueToken(TYPE_INT); }
"long"          { return valueToken(TYPE_LONG); }
"float"          { return valueToken(TYPE_FLOAT); }
"double"          { return valueToken(TYPE_DOUBLE); }
"bool"          { return valueToken(TYPE_BOOL); }
"char"          { return valueToken(TYPE_CHAR); }
"def"          { return valueToken(DEF); }
"public"          { return token(VISIBILITY_PUBLIC); }
"protected"          { return token(VISIBILITY_PROTECTED); }
"internal"          { return token(VISIBILITY_INTERNAL); }
"private"          { return token(VISIBILITY_PRIVATE); }
"final"          { return token(FINAL); }
"static"          { return token(STATIC); }
"package"          { return token(PACKAGE); }
"class"          { return token(CLASS); }
"extends"          { return token(EXTENDS); }
"implements"          { return token(IMPLEMENTS); }
"extension"          { return token(EXTENSION); }
"fun"          { return token(FUN); }
"return"          { return token(RETURN); }
"import"          { return token(IMPORT); }
"as"          { return token(AS); }
"inline"          { return token(INLINE); }
"new"          { return token(NEW); }
"for"          { return token(FOR); }
"in"          { return token(IN); }
"if"          { return token(IF); }
"switch"          { return token(SWITCH); }
"else"          { return token(ELSE); }
"while"          { return token(WHILE); }
"null"          { return token(NULL); }
"break"          { return token(BREAK); }
"continue"          { return token(CONTINUE); }

{IDENTIFIER} {  return valueToken(IDENTIFIER); }

// symbols
"("          { return token(LPAR); }
")"          { return token(RPAR); }
"{"          { return token(BRACKETS_OPEN); }
"}"          { return token(BRACKETS_CLOSE); }
"["          { return token(SQUARE_BRACKETS_OPEN); }
"]"          { return token(SQUARE_BRACKETS_CLOSE); }
":"          { return token(COLON); }
";"          { return token(SEMI_COLON); }
"+"          { return token(PLUS); }
"-"          { return token(MINUS); }
"/"          { return token(DIV); }
"*"          { return token(MUL); }
"%"          { return token(MODULO); }
"!"          { return token(NOT); }
"?"          { return token(QUESTION_MARK); }
"<<"          { return token(LEFT_SHIFT); }
">>"          { return token(RIGHT_SHIFT); }
"&&"          { return token(AND); }
"||"          { return token(OR); }
">="          { return token(GOE); }
">"          { return token(GT); }
"<="          { return token(LOE); }
"<"          { return token(LT); }
"=="          { return token(EQUAL); }
"!="          { return token(NOT_EQUAL); }
"="          { return token(ASSIGNMENT); }
"+="          { return token(PLUS_ASSIGNMENT); }
"-="          { return token(MINUS_ASSIGNMENT); }
"*="          { return token(MUL_ASSIGNMENT); }
"/="          { return token(DIV_ASSIGNMENT); }
".."          { return token(TWO_DOTS); }
"?."          { return token(QUESTION_DOT); }
"."          { return token(DOT); }
","          { return token(COMMA); }
"++"          { return token(INCR); }
"--"          { return token(DECR); }
"->"          { return token(ARROW); }