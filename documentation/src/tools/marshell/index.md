# Marshell

Marshell is a shell that can be used to run marcel instructions, on the fly. It is
a Read Eval Print Loop tool.

It is the equivalent of [groovysh](https://groovy-lang.org/groovysh.html) for Marcel.

This shell supports syntax highlighting and also highlights defined functions/variables.

## Global variables
In marshell, you can't have [class fields](../../language-specification/source-file-structure/script.md#fields), **but** you can use global variables.
To declare a global variable, just assign to a variable a value, without specifying its type

```marcel
a = 1 // this will create a global variable a
```

We didn't declare the variable `a`. If we were in a regular class/source file this wouldn't compile, but in Marshell this is possible.

The type of the global variable is determined by the value provided, and it can't change type. Meaning that after having defining one, you can't assign to it
a value that is of an incompatible type of the one you used when you first assigned it a value.

```marcel
a = 1

doSomething(a)

a = "2" // Semantic Error: Expected expression of type int but gave String
```

To specify explicitly the type of the global variable, use the `as` keyword.

```marcel
a = [1, 2] as Set<int>
```

### How global variables works
Global variables are variables that are stored in the script's [Binding](https://github.com/tambapps/marcel/blob/main/marcel-stdlib/src/main/java/marcel/lang/Binding.java). The means you could also
retrieve them/set them using methods like `Script.getVariable(name)`/`Script.setVariable(name, value)`


## Define functions

Define functions as you would in a Marcel script

## Define classes

Define classes as you would in a Marcel script. All defined classes are top-level classes (they
are not inner class as they would be in a Marcel script).

## Run commands

Marshell has some specific commands make your experience even better.

Use the `:help` command to see all the commands (marshell-specific instructions) you can run

## Import dependencies
You can import dependencies on the fly with the `:pull` command

```text
marshell:000> :pull com.google.code.gson:gson:2.10.1
marshell:000> :import com.google.gson.Gson
marshell:000> gson = new Gson()
```

## Initialisation script
If you want to always load some data everytime you run marshell, you can create a
script in `$MARCEL_HOME/marshell/init.mcl`.

In this script you **can't** use commands. If you want to import a dependency/dumbbell,
just do it like you would in a normal marcel script (`import ...` or `dumbbell '...'`)