# Installation

Marcel comes with
- [marcl](../tools/marcl.md)
- [marshell](../tools/marshell/index)
- [dumbbell](../tools/dumbbell.md)


## Install from release
### Using the script
You can run the below command in order to install a Marcel release in a specific directory.

```shell
curl -s https://raw.githubusercontent.com/tambapps/marcel/main/install/install-from-release.sh | bash -s
```

It will install Marcel in the `$HOME/.marcel` folder. 

You can also specify a directory by passing it as an argument as shown below
```shell
# Will install in ./my-folder
curl -s https://raw.githubusercontent.com/tambapps/marcel/main/install/install-from-release.sh | bash -s ./my-folder
```

### Downloading the release

You can download the `release.zip` of a [Marcel Release](https://github.com/tambapps/marcel/releases). Unzip it where you want.

### Set MARCEL_HOME
Set the `MARCEL_HOME` environment to the path of the release you just unzipped. You can set it in your `.bashrc` or `.zshrc` so that
this variable is set in all your sessions.
You can also add `$MARCEL_HOME/bin` to your `PATH` so your shell recognize marcel commands.

## Install from source code
There is a [script in marcel repository](https://github.com/tambapps/marcel/blob/main/install.sh) for that.

Note that this script **only works on Linux and Mac** and requires [Maven](https://maven.apache.org/) being installed.
```shell
git clone https://github.com/tambapps/marcel.git
cd marcel
./install/install-from-source.sh
```

### Prerequisites

You'll need Maven for the installation.

### Run script

Clone the repository
```shell
git clone https://github.com/tambapps/marcel.git
cd marcel
```

And then run the script
```shell
./install-from-source.sh
```

The script basically runs a lot of `mvn clean package` and then copy/create some files in `$HOME/.marcel/`.

Lastly, you can add the following lines into your `$HOME/.bashrc` (or `$HOME/.zshrc` or whatever) to easily use marcel tools

```shell
MARCEL_HOME="$HOME/.marcel"
PATH="$PATH:$MARCEL_HOME/bin"
```
