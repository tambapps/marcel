# Extension classes

Extension classes allows you to add methods to an existing class.

## How to declare
```groovy
// MyExtension.mcl
extension class MyExtension for Integer {
  fun Integer next() {
    return this + 1
  }
}
```

The above example will add the method `next()` to the class Integer.

In such classes you can reference, you can only reference public properties/methods of the class you're extending, and you have
to explicitly specify `this` when you do so.
E.g.

```groovy
// MyExtension.mcl
extension class MyExtension for Integer {
  fun float nextToFloat() {
    return this.floatValue() + 1f
  }
}
```
## How to use

Import your extension class with the `extension` keyword.


```groovy
// another file
import extension MyExtension

Integer a = 1
println(a.next())
```