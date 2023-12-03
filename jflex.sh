#!/bin/zsh
#Run Jflex, the tool to autogenerate the lexer
java -Xmx512m -Dfile.encoding=UTF-8 -jar marcel-lexer/src/main/java/com/tambapps/marcel/lexer/jflex-1.7.0-2.jar \
   -skel marcel-lexer/src/main/java/com/tambapps/marcel/lexer/idea-flex.skeleton \
    -d marcel-lexer/src/main/java/com/tambapps/marcel/lexer marcel-lexer/src/main/flex/Marcel.flex
