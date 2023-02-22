# Marcl

MarCL (MARcel Command Line tool) is useful to compile/execute marcel source files.

Let's explore all the commands it provides

## Execute

This is the default command, meaning that if you don't specify a command, it will
use this one.

```text
Usage: marcl execute [OPTIONS] FILE [SCRIPT_ARGUMENTS]...

  Execute a marcel script

Options:
  -c, --keep-class         keep compiled class files after execution
  -j, --keep-jar           keep compiled jar file after execution
  -p, --print-stack-trace  print stack trace on compilation error
  -h, --help               Show this message and exit
```

### Examples
```shell
marcl execute script.mcl
```

```shell
marcl -c script.mcl
```

```shell
marcl execute -cj script.mcl myScriptArg1 myScriptArg2
```

## Compile

```text
Usage: marcl compile [OPTIONS] FILE

  Compiles a Marcel class to a .class file and/or .jar file

Options:
  -c, --class              Compile to class
  -j, --jar                Compile to jar
  -p, --print-stack-trace  print stack trace on compilation error
  -h, --help               Show this message and exit

```

### Examples
```shell
marcl compile script.mcl
```

```shell
marcl compile -cj script.mcl
```

