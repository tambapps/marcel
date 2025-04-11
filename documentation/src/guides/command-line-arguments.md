# Command Line options/arguments

Marcel provides utilities to parse command-line options and arguments.

All classes referenced in this guide are available under the package `marcel.io.clargs`.

## Define options

An option can be defined as a field, annotated with `@Option`.

```marcel
@Option
private int depth;
```

The above piece of code declares an option named `depth`.

### Specify option names
You can specify short and/or long name of your option through the annotation.

```marcel
@Option(shortName = "d", longName = "depth")
private int depth
```
The difference between short and long names are how you specify the option when running the script.
Short name are to be specified with only one dash, whereas long names are to be specified with 2.

E.g.
```shell
marcl script.mcl -d 1
marcl script.mcl --depth 1
```

When no longName was specified, Marcel fallbacks to the name of the annotated field.

### Optional options and default values
By default an option is **required**. You can change this behaviour by marking it as `required = false` **or**
by giving it a default value.

```marcel
@Option(required = false)
private int depth

@Option
private int depth2 = 5 // not required as it has a default value
```
As you can see above, you can specify the default value of an option by assigning a value to a field.

Note that for `null` or 0 values, you'll still need to explicitly specify the `required = false` flag in order for the option to be optional.

### Multivalued options
Sometimes you may want an option to be specifiable multiple times. In such case you'll need to declare the field as a Collection.
List and Sets ([even of primitives](../language-specification/types/collections-of-primitives.md)) are supported.

for example the below example allows to specify multiple `depth`

```marcel
@Option(shortName = "d", longName = "depth", arity = "*")
private List<int> depths
```

The `arity` annotation property is a String specifying how many arguments there can be for the given option (defaults to 1).
It supports many formats:
- `*` corresponds to any arity (0, or more)
- a range, like 2..5 (from 2 inclusive to 5 inclusive)
- a range with one infinite bound. E.g. 2..* for at least 2, or *..4 for at most 4
- a number n followed be a '+' to specify at least n (e.g. 5+)
- a number, for exactly n arguments

### Conversion
Marcel supports argument of the following types
- all primitive types and their object wrapper class
- String
- BigInteger
- BigDecimal
- File
- Path
- Enum classes

You can declare an option field of one of the above type, and Marcel will
automatically know how to convert the option's value (String) into the target type.

For other types, you can specify a `converter` as shown below
```marcel
@Option(converter = { String s -> Duration.parse(s) })
private Duration duration
```
You can throw an `IllegalArgumentException` in those lambda to specify that the option value is incorrect.

### Multivalued options
for Multi-valued options, Marcel supports
- List
- Set
- all [list/set of primitives](../language-specification/types/collections-of-primitives.md)

But as Marcel does not support generic types if you declare a field as a List/Set (Java's List/Set, not of primitives),
it will put the raw value as a String in it.

You can still specify a custom `converter` for such options, it will be applied on **each** value of the option.

Let's look at the below options
```marcel
@Option(longName = "int", arity = '*') // no need for a converter as it is a list of primitive ints
private List<int> ints
@Option(longName = "duration", arity = '*', converter = { String s -> Duration.parse(s) })
private List durations
```

For the command
```shell
 marcl Test.marcel -duration PT1S PT1M -int 1 -int 2 -int 3
```

We will have `ints = [1, 2, 3]` and `durations = [PT1S, PT1M]`.

#### Value separator
As seen above, we provide multiple values to a multivalued option by using multiple times the same option, 
but there is another way to pass those: using a `valueSeparator`.

The value separator will force the user to pass all the values of the option at once, separated by the `valueSeparator`.

For example, the option
```marcel
@Option(longName = "int", arity = '*', valueSeparator = ',')
private List<int> ints
```

with the command
```shell
 marcl Test.marcel -int 1,2,3
```

Will result in `ints = [1, 2, 3]`.

### Validator
Sometimes you may want to add validation to an option. You can do so by specifying
a lambda under the `validation` annotation parameter. Any `IllegalArgumentException` thrown in this lambda
will be considered as a validation error.


```marcel
@Option(shortName = "n", validator = { int n ->
    if (n % 2 != 0) throw new IllegalArgumentException("n should be even")
  })
private int number
```

## Define arguments

The arguments of your programs are the value passed, not assigned to any options. For example, in the example below
```
marcl script.mcl -d 1 -n "a value" arg1 arg2 arg3
```
There is
- the option `d=1`
- the option `n="a value"`
- the arguments `arg1`, `arg2`, `arg3`

You can assign these arguments values to a field thanks to the `@Arguments` annotation.

```marcel
@Arguments
private List args
```

## Parse options

After having declared the options and/or arguments, we want to assign them values based on the command line arguments passed
to execute the script.

This can be done with one line

```
ClArgs.init(this, args)
```

This line of code will parse the command line `args` and assign the appropriate values to your options.

In more details it will
- parse the command line arguments
- if any error occurred (e.g. arity not respected, type not respected, validation error): print the error, print the usage of this script, and then exit (`System.exit(1)`)
- if no error occurred, you option fields are ready to be used

### Help option
You can use the `@HelpOption` annotation on a `boolean` field to specify an option that should print usage and then exit the program 
(this behaviour is handled automatically by the `ClArgs.init(...)` method), as shown below.

```marcel
@HelpOption
private boolean help
```

## Document the usage of your script

We've seen that the usage of your script will be printed in case of error. 
You can provide more data to annotations and the `ClArgs.init(...)` method call in order to print a well explained usage.

### Option description

Use the `description` annotation parameter to describe what your option does

```
@Option(description = "the max depth at which to search files")
private int depth
```

### Customize usage

The `ClArgs.init(...)` has several optional arguments allowing to enrich the `usage` message.

#### usage
Usage summary displayed as the first line.

#### header
Optional additional message for usage; displayed after the usage summary but before the options are displayed.


#### footer
Optional additional message for usage; displayed after the options are displayed.


## Full example

Take a look at the below script.

```marcel
import marcel.io.clargs.*
import java.time.Duration

@Option(shortName = "d", validator = { int d ->
    if (d <= 0) throw new IllegalArgumentException("should be positive")
  }, description = "The depth limit of the search")
private int depth = 100

@Option(shortName = "n", description = "The name to search")
private String name

@Option(shortName = "i", longName = "ignore-case", description = "whether to ignore case when matching the name", required = false)
private boolean ignoreCase


@HelpOption
private boolean help

@Arguments
private List filePaths

ClArgs.init(this, args, usage: 'marcl script.mcl')

// process options and arguments
```

The usage of this script would result as shown below

```text
usage: marcl script.mcl ARGUMENTS
 -d,--depth <arg>   The depth limit of the search. default: 100
 -h,--help          Prints usage information
 -i,--ignore-case   whether to ignore case when matching the name. default: false
 -n,--name <arg>    The name to search
```