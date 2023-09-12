# Installation

Marcel comes with
- [marcl](../tools/marcl.md)
- [marshell](../tools/marshell.md)
- [dumbbell](../tools/dumbbell.md)

For now, the only way to install Marcel is compiling it from its source code.

## Install from release
You can download the `release.zip` of a [Marcel Release](https://github.com/tambapps/marcel/releases). Unzip it where you want.

Then set the `MARCEL_HOME` environment to the path of the release you just unzipped. You can set it in your `.bashrc` or `.zshrc` so that
this variable is set in all your sessions.
You can also add `$MARCEL_HOME/bin` to your `PATH` so your shell recognize marcel commands.

## Install from source code
There is a [script in marcel repository](https://github.com/tambapps/marcel/blob/main/install.sh) for that.

Note that this script **only works on Linux and Mac**.
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
