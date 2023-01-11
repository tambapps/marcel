package com.tambapps.marcel.lexer;

import com.tambapps.marcel.lexer.TokenType;


/**
  * Marcel lang lexer
  */
%%
%class GarconJflexer
%unicode
%cup
%line
%column

DIGIT=[0-9]
WHITE_SPACE_CHAR=[\ \n\t\f]

%%

"/**/" {
    return TokenType.BLOCK_COMMENT;
}
