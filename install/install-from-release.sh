#!/bin/bash

set -e # exit on any command failure

MARCEL_RELEASE=0.0.1 # Update the release number you want here
if ! command -v wget &> /dev/null
then
    echo "Error: wget must be installed"
    exit 1
fi

if [ "$#" -gt 0 ]; then
  mkdir -p $1
  marcelDir=$(realpath $1)
else
  marcelDir="$HOME/.marcel"
fi

MARCEL_LIB_FOLDER=$marcelDir/lib
MARCEL_BIN_FOLDER=$marcelDir/bin
mkdir -p $MARCEL_LIB_FOLDER
mkdir -p $MARCEL_BIN_FOLDER

wget -q https://github.com/tambapps/marcel/releases/download/v${MARCEL_RELEASE}/release.zip -O release.zip
unzip -qq -j release.zip -d $MARCEL_LIB_FOLDER
rm release.zip

########################
# Creating Executables #
########################
create_executable() {
  cat <<EOF > $marcelDir/bin/$1
#!/bin/bash
java -cp '$marcelDir/lib/*' $2 "\$@"
EOF
  chmod u+x $MARCEL_BIN_FOLDER/$1
}

create_executable marcl 'com.tambapps.marcel.cl.MarClKt'
create_executable marshell 'com.tambapps.marcel.marshell.MarshellKt'
create_executable dumbbell 'com.tambapps.marcel.dumbbell.cl.DumbbellKt'

echo "Marcel has been successfully installed in $marcelDir."
echo "Set the MARCEL_HOME environment variable to $marcelDir so that Marcel tools are aware of where the home is"
echo "Add $marcelDir/bin in your PATH for your shell to recognize Marcel commands"