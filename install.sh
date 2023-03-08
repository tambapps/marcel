#!/bin/bash

set -e # exit on any command failure

if ! command -v mvn &> /dev/null
then
    echo "Error: mvn must be installed"
    exit 1
fi

if [ "$#" -gt 0 ]; then
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

echo "Installing marcel lexer, parser, and stdlib"
mvn install -N # install parent pom only
mvn clean install -pl marcel-lexer,marcel-stdlib,marcel-parser
cp marcel-lexer/target/marcel-lexer-$version.jar $marcelDir/lib/marcel-lexer.jar
cp marcel-stdlib/target/marcel-stdlib-$version.jar $marcelDir/lib/marcel-stdlib.jar
cp marcel-parser/target/marcel-parser-$version.jar $marcelDir/lib/marcel-parser.jar


echo "Installing dumbbell (needed for marcel-compiler)"
# Dumbbell
cd subprojects/dumbbell
mvn clean install
cp target/dumbbell-$version.jar $marcelDir/lib/dumbbell.jar

# Dumbbell CL
cd ../dumbbell-cl
mvn clean install
cp target/dumbbell-cl-$version.jar $marcelDir/lib/dumbbell-cl.jar

echo "Installing marcel compiler"
cd ../..
mvn install -pl marcel-compiler
cp marcel-compiler/target/marcel-compiler-$version.jar $marcelDir/lib/marcel-compiler.jar

echo "Installing MarCL"
mvn install -pl marcl
cp marcl/target/marcl-$version.jar $marcelDir/lib/marcl.jar

echo "Installing marshell"
cd subprojects/marcel-repl
mvn clean install
cp target/marcel-repl-$version.jar $marcelDir/lib/marcel-repl.jar

cd ../marshell
mvn clean install
cp target/marshell-$version.jar $marcelDir/lib/marshell.jar

# Marcel libs used to be included in classpath when running marcel command line tools
cd ../marcel-libs
mvn clean install
cp target/marcel-libs-$version-jar-with-dependencies.jar $marcelDir/lib/marcel-libs.jar

########################
# Creating Executables #
########################
create_executable() {
  cat <<EOF > $marcelDir/bin/$1
#!/bin/bash
java -cp '/home/nfonkoua/.marcel/lib/*' $2 "\$@"
EOF
  chmod u+x $marcelDir/bin/$1
}

create_executable marcl 'com.tambapps.marcel.cl.MarClKt'
create_executable marshell 'com.tambapps.marcel.marshell.MainKt'
create_executable dumbbell 'com.tambapps.marcel.dumbbell.cl.DumbbellKt'

echo "Marcel has been installed in $marcelDir."
echo "Add $marcelDir/bin in your PATH to be able to use marcel tools"