#!/bin/zsh
#Run Jflex, the tool to autogenerate the lexer
java -Xmx512m -Dfile.encoding=UTF-8 -jar src/main/kotlin/com/tambapps/marcel/lexer/jflex-1.7.0-2.jar \
   -skel src/main/kotlin/com/tambapps/marcel/lexer/idea-flex.skeleton \
    -d src/main/kotlin/com/tambapps/marcel/lexer src/main/flex/Marcel.flex
