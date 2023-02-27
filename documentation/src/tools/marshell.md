# Marshell


Marshell is a shell that can be used to run marcel instructions, on the fly. It is
a Read Eval Print Loop tool.


It is the equivalent of [groovysh](https://groovy-lang.org/groovysh.html) for Marcel.

This shell supports syntax highlighting and also highlights defined functions/variables.


## Define variables
In marshell, you can't have [class fields](../language-specification/source-file-structure/script.md#fields), **but** you can use global variables.
To de clare a global variable, just assign to a variable a value, without specifying its type

```groovy
a = 1 // this will create a global variable a
```

Global variables can't change type. Meaning that after having defining one, you can't assign to it
a value that is of an incompatible type of the one you used when you first assigned it a value.

To specify explicitly the type of the global variable, use the `as` keyword.

```groovy
a = [1, 2] as set<int>
```

## Define functions

Define functions as you would in a Marcel script
## Define classes

Define classes as you would in a Marcel script. All defined classes are top-level classes (they
are not inner class as they would be in a Marcel script).

## Run commands

Marshell has some specific commands make your experience even better.


Use the `:help` command to see all the commands (marshell-specific instructions) you can run


### Import dependencies
You can import dependencies on the fly with the `:pull` command

```text
marshell:000> :pull com.google.code.gson:gson:2.10.1
marshell:000> :import com.google.gson.Gson
marshell:000> gson = new Gson()
```