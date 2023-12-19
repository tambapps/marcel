# Meta-annotations (metaprogramming with annotations)
[AST transformations](./index.md) can be specified on annotations. Doing so allows to perform specific transformations when annotating a given
class, field, method and/or field.

Marcel's standard library provides many annotations useful to avoid writing boilerplate code. Some of them are similar as the one you could find in [Lombok](https://projectlombok.org/).

Meta-annotations from the Marcel's standard-library are all lowercase (even the first letter), this is how you can differentiate them from
other (non-meta) annotations.

## @stringify

This annotation is similar to [Lombok's @ToString annotation](https://projectlombok.org/features/ToString).
It auto-generates a `toString()` method for your class, based on your class's members.

The generated toString method will put all fields of the class with their values in the generated String.

```marcel
@stringify
class Foo {
 String bar = "myBar"
}

println(new Foo())
```

This script would print `Foo(bar=myBar)`. 

### Include getters
The getters are not included in the generated String by default. To change this behaviour, you can use the flag `includeGetters=true`.

```marcel
@stringify(includeGetters=true)
class Foo {
 String bar = "myBar"
 
 fun int getZoo() -> 5 
}

println(new Foo())
```

This script would print `Foo(bar=myBar, zoo=5)`. 

### Exclude particular fields/methods
You can use the annotation `@stringify.Exclude` to exclude a particular field or getter.

```marcel
@stringify(includeGetters=true)
class Foo {
 int i = 1

 @stringify.Exclude
 String b = "srsr"

 @stringify.Exclude
 fun String getFoo() -> "foo"

 fun String getBar() -> "bar"
}
```

This script would print `A(i=1, bar=bar)`. 
