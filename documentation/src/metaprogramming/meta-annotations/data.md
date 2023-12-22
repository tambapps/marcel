# @data

This annotation is similar to [Lombok's @Data annotation](https://projectlombok.org/features/Data).
It auto-generates the `equals()`, `hashCode()` and `toString()` method for your class, based on your class's members.

The 

You can also make your class [implement Comparable](./comparable.md) by providing the `comparable=true` flag

```marcel
@data
class Foo {
  int a = 2
  String b = "b"
}
```

## Generate an all args constructor

You can generate a constructor for your class using the `withConstructor` flag.

```marcel
@data(withConstructor=true)
class Foo {
  int a
  String b
}
println(new Foo(1, "b"))
```
This code will generate the constructor `constructor(this.a, this.b)`.

To omit a particular field for the generated constructor, annotate the field with `@data.Exclude`.

```marcel
@data(withConstructor=true)
class Foo {
  int a
  @data.Exclude
  String b
}

println(new Foo(1))
```
This code will generate the constructor `constructor(this.a)`.

### Exclude particular fields/methods
You can use the `@data.Exclude`, `@comparable.Exclude` or `@stringify.Exclude` annotations to exclude properties from the sring representation,

`@data.Exclude` will exclude the property from both the `toString()`, and the `equals()`,`hashCode()` methods.
`@comparable.Exclude` will only exclude the property for the `equals()`,`hashCode()` methods.
`@stringify.Exclude` will only exclude the property for the `toString()` method.

```marcel
@data
class Foo {
 @data.Exclude
 int i = 1

 @comparable.Exclude
 String b = "srsr"

 @comparable.Exclude
 fun String getFoo() -> "foo"

 fun String getBar() -> "bar"
}
```