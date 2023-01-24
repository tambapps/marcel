package com.tambapps.marcel.lexer;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
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
    try (Reader reader = new StringReader(content)) {
      return lex(reader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<LexToken> lex(Reader content) throws IOException, MarcelLexerException {
    MarcelJflexer jflexer = new MarcelJflexer(content);
    List<LexToken> tokens = new ArrayList<>();
    LexToken token;
    while ((token = jflexer.nextToken()) != null) {
      TokenType type = token.getType();
      if (!COMMENT_TOKENS.contains(type) && (!ignoreWhitespaces || type != TokenType.WHITE_SPACE)) {
        tokens.add(token);
      }
    }
    tokens.add(new LexToken(TokenType.END_OF_FILE));
    return tokens;
  }

}