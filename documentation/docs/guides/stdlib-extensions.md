# Standard Library extensions

In this section we'll take a look at the extensions Marcel stdlib provides.

To understand how extensions work, you can consult the [related page](../language-specification/extension-classes.md).

## Automatically imported extensions

Marcel imports by default a large panel of extensions, which you can consult under the
[DefaultMarcelMethods class](https://github.com/tambapps/marcel/blob/main/marcel-stdlib/src/main/java/marcel/lang/methods/DefaultMarcelMethods.java).

There are also methods defined in separate classes.

### Character extensions

You can call static methods from `Character` class as if they were instance methods, even on primitive `char` values.

```marcel
println(`c`.isLowerCase()) // true

Character foo = `s`
println(foo.isDigit()) // false
```

### CharSequence Extensions

There are some extensions on classes implementing `CharSequence` to handle them like a collection of characters.
```marcel
CharSequence foo = "a simple string 2"
println(foo[0])
List<char> charList = foo.toList()

int index = foo.indexOf { it == ` ` }
int lastIndex = foo.lastIndexOf { it == `s` }

if (Character c = foo.find { it.isDigit() }) {
  println("There is a digit char: '$c'")
}
println("There are " + foo.count { it == `s` } + " 's'")
if (foo.any { it.isWhitespace() }) {
  println("The string contains spaces")
}
```

And other useful methods

```marcel
CharSequence foo = "string"
println(foo.reversed())

println("123".toInt()) // there is also toDouble, toFloat, toLong
```

## Other extensions
There are other extensions in Marcel's Standrard Library you can use, by explicitely importing them. E.g.

````
import extension marcel.lang.extensions.TimeExtensions
````

### Time Extensions

The [TimeExtensions class](https://github.com/tambapps/marcel/blob/main/marcel-extensions/src/main/java/marcel/lang/extensions/TimeExtensions.java)
provides utilities to construct durations easily.

Let's take a look at the below example

```marcel
import extension marcel.lang.extensions.TimeExtensions

println(1.days)
println(2.hours)
println(1.minutes)
println(1.seconds)
println(1.millis)
println(1.nanos)
```

You can access a "field" from the `int`, `Integer`, `long`, `Long` classes to create a duration. Of course this isn't a real field, as
primitive types don't have any, but an extension provided by the `TimeExtensions` class.