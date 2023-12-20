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