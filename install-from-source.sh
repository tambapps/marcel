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
mvn clean package # TODO find a way to skip dokka
cp marcel-lexer/target/marcel-lexer-$version.jar $marcelDir/lib/marcel-lexer.jar
cp marcel-stdlib/target/marcel-stdlib-$version.jar $marcelDir/lib/marcel-stdlib.jar
cp marcel-parser/target/marcel-parser-$version.jar $marcelDir/lib/marcel-parser.jar
cp marcel-compiler/target/marcel-compiler-$version.jar $marcelDir/lib/marcel-compiler.jar
cp marcel-extensions/target/marcel-extensions-$version.jar $marcelDir/lib/marcel-extensions.jar
cp marcl/target/marcl-$version.jar $marcelDir/lib/marcl.jar
cp subprojects/dumbbell-core/target/dumbbell-core-$version.jar $marcelDir/lib/dumbbell-core.jar
cp subprojects/dumbbell/target/dumbbell-$version.jar $marcelDir/lib/dumbbell.jar
cp subprojects/marcel-repl/target/marcel-repl-$version.jar $marcelDir/lib/marcel-repl.jar
cp subprojects/marshell/target/marshell-$version.jar $marcelDir/lib/marshell.jar
# Marcel libs used to be included in classpath when running marcel command line tools
cp subprojects/marcel-libs/target/marcel-libs-$version-jar-with-dependencies.jar $marcelDir/lib/marcel-libs.jar

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