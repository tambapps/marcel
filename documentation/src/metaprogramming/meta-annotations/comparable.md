# @comparable

This makes your class implement [Comparable](https://docs.oracle.com/javase/8/docs/api/java/lang/Comparable.html) interface. The comparison will be made based on your class's field, in the order
in which they were defined.

For example take a look at the below class

```marcel
@comparable
class Foo {
  int a
  String b
  double c
}
```

When comparing 2 Foo instances, we'll start by comparing the fields `a`. If they are not the same (one is greater/lower than the other),
we'll stop the comparison here as we already can determine which Foo instance is greater than the other. If both `a` fields hold the same value,
we'll continue the comparison with `b`, and so on and so on...

## Include getters
The getters are not included in the generated Comparison by default. To change this behaviour, you can use the flag `includeGetters=true`.

### Exclude particular fields/methods
You can use the annotation `@comparable.Exclude` to exclude a particular field or getter.

```marcel
@comparable(includeGetters=true)
class Foo {
 int i = 1

 @comparable.Exclude
 String b = "srsr"

 @comparable.Exclude
 fun String getFoo() -> "foo"

 fun String getBar() -> "bar"
}
```