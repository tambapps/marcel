package com.tambapps.marcel.lexer;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@AllArgsConstructor
public class MarcelLexer {

  private static final List<TokenType> COMMENT_TOKENS = Arrays.asList(
      TokenType.BLOCK_COMMENT, TokenType.DOC_COMMENT, TokenType.HASH, TokenType.SHEBANG_COMMENT, TokenType.EOL_COMMENT
  );
  private final boolean ignoreWhitespaces;

  public MarcelLexer() {
    this(true);
  }

  public List<LexToken> lex(String content) throws MarcelLexerException {
    return lex(content, false);
  }

  public List<LexToken> lexSafely(String content) throws MarcelLexerException {
    return lex(content, true);
  }

  @SneakyThrows
  private List<LexToken> lex(String content, boolean catchAndStop) throws MarcelLexerException {
    MarcelJflexer jflexer = new MarcelJflexer();
    jflexer.reset(content, 0, content.length(), MarcelJflexer.YYINITIAL);
    List<LexToken> tokens = new ArrayList<>();
    LexToken token;
    while (true) {
      try {
        token = jflexer.nextToken();
      } catch (MarcelJfexerException e) {
        if (catchAndStop) {
          return tokens;
        }
        int line = jflexer.getYyline();
        int column = jflexer.getYycolumn();
        String message = e.getMessage();
        if (jflexer.getZzLexicalState() == MarcelJflexer.STRING
            || jflexer.getZzLexicalState() == MarcelJflexer.SIMPLE_STRING
            || jflexer.getZzLexicalState() == MarcelJflexer.CHAR_STRING
            || jflexer.getZzLexicalState() == MarcelJflexer.RAW_STRING
            || jflexer.getZzLexicalState() == MarcelJflexer.REGEX_STRING) {
          message = "String is malformed";
        }
        throw new MarcelLexerException(line, column, message);
      }
      if (token == null) break;
      if (token.getType() == TokenType.BAD_CHARACTER) {
        throw new MarcelLexerException(token.getLine(), token.getColumn(), "Bad character " + token.getValue());
      }
      if (!COMMENT_TOKENS.contains(token.getType()) && (!ignoreWhitespaces || token.getType() != TokenType.WHITE_SPACE)) {
        tokens.add(token);
      }
    }
    tokens.add(new LexToken(content.length(), content.length(),
        jflexer.getYyline(), jflexer.getYycolumn(), TokenType.END_OF_FILE, null));
    return tokens;
  }
}