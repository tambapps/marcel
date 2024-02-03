#!/bin/bash

set -e # exit on any command failure

if ! command -v mvn &> /dev/null
then
    echo "Error: mvn must be installed"
    exit 1
fi

if [ "$#" -gt 0 ]; then
  mkdir -p $1
  marcelDir=$(realpath $1)
else
  marcelDir="$HOME/.marcel"
fi

if [ -f "$marcelDir" ]
then
    echo "Cannot create marcel directory: $marcelDir is a regular file"
    exit 2
elif [ ! -d "$marcelDir" ]
then
    mkdir $marcelDir
fi

mkdir -p $marcelDir/lib
mkdir -p $marcelDir/bin

version=$(mvn org.apache.maven.plugins:maven-help-plugin:3.2.0:evaluate -Dexpression=project.version -q -DforceStdout)

echo "Installing marcel..."
mvn clean package -Djavadoc.skip=true

# compilation modules
cp marcel-compilation/marcel-lexer/target/marcel-lexer-$version.jar $marcelDir/lib/marcel-lexer.jar
# TODO fix semantic
cp marcel-compilation/marcel-semantic/target/marcel-semantic-$version.jar $marcelDir/lib/marcel-semantic.jar
cp marcel-compilation/marcel-parser/target/marcel-parser-$version.jar $marcelDir/lib/marcel-parser.jar
cp marcel-compilation/marcel-compiler/target/marcel-compiler-$version.jar $marcelDir/lib/marcel-compiler.jar

# stdlib modules
cp marcel-stdlib/target/marcel-stdlib-$version.jar $marcelDir/lib/marcel-stdlib.jar
cp marcel-extensions/target/marcel-extensions-$version.jar $marcelDir/lib/marcel-extensions.jar

# subprojects
cp marcel-subprojects/marcl/target/marcl-$version.jar $marcelDir/lib/marcl.jar
cp marcel-subprojects/dumbbell-core/target/dumbbell-core-$version.jar $marcelDir/lib/dumbbell-core.jar
cp marcel-subprojects/dumbbell/target/dumbbell-$version.jar $marcelDir/lib/dumbbell.jar
cp marcel-subprojects/marcel-repl/target/marcel-repl-$version.jar $marcelDir/lib/marcel-repl.jar
cp marcel-subprojects/marshell/target/marshell-$version.jar $marcelDir/lib/marshell.jar
cp marcel-subprojects/threadmill/target/threadmill-$version.jar $marcelDir/lib/threadmill.jar
cp marcel-subprojects/marcel-deps/target/marcel-deps-$version-jar-with-dependencies.jar $marcelDir/lib/marcel-deps.jar

########################
# Creating Executables #
########################
create_executable() {
  cat <<EOF > $marcelDir/bin/$1
#!/bin/bash
java -cp '$marcelDir/lib/*' $2 "\$@"
EOF
  chmod u+x $marcelDir/bin/$1
}

create_executable marcl 'com.tambapps.marcel.cl.MarClKt'
create_executable marshell 'com.tambapps.marcel.marshell.MarshellKt'
create_executable dumbbell 'com.tambapps.marcel.dumbbell.cl.DumbbellKt'

echo "Marcel has been successfully installed in $marcelDir."
echo "Set the MARCEL_HOME environment variable to $marcelDir so that Marcel tools are aware of where the home is"
echo "Add $marcelDir/bin in your PATH for your shell to recognize Marcel commands"