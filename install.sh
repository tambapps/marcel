#!/bin/bash

if ! command -v mvn &> /dev/null
then
    echo "Error: mvn must be installed"
    exit
fi

echo "Installing marcel compiler"
mvn clean install -pl marcel-lexer,marcel-stdlib,marcel-parser,marcel-compiler || exit 1

echo "Installing dumbbell (needed for MarCL)"
cd subprojects/dumbbell || exit 1
mvn clean install || exit 1

echo "Installing MarCL"
cd ../.. || exit 1
mvn clean install -pl marcl || exit 1

echo "Installing marshell"
cd subprojects/marcel-repl || exit 1
mvn clean install || exit 1
cd ../marshell || exit 1
mvn clean install || exit 1