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

@Option(required = false)
private int depth2 = 5 // not required as it has a default value
```
As you can see above, you can specify the default value of an option by assigning a value to a field.

Note that fir `null` or 0 vakues, you'll need to explicitely specify the `required = false` flag in order for the option to be optional.

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

You can declare an option field of one of the above type, and Marcel will
automatically know how to convert the option's value (String) into the target type.

For other types, you can specify the `converter`, a Lambda TODO EXPLAIN LAMBDA AND WHY IT IS DECLARED AS A CLASS FIELD

#### Multivalued option conversion
for Multi-valued types Marcel supports
- List
- Set
- all [list/set of primitives](../language-specification/types/collections-of-primitives.md)

But as Marcel does not support generic types if you declare a field as a List/Set (Java's List/Set, not of primitives),
it will put the raw value as a String in it.

To have elements of the wanted type, you can specify the `converter`, which will be applied on **each**
value of the option.

