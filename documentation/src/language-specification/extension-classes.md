# Extension classes

Extension classes allows you to add methods to an existing class.

## How to declare
```marcel
// MyExtension.mcl
extension class MyExtension for Integer {
  fun Integer next() {
    return this + 1
  }
}
```

The above example will add the method `next()` to the class Integer.


You can also call methods from the original class

```marcel
// MyExtension.mcl
extension class MyExtension for Integer {
  fun float nextToFloat() {
    return floatValue() + 1f
  }
}
```
## How to use

Import your extension class with the `extension` keyword.


```marcel
// another file
import extension MyExtension

Integer a = 1
println(a.next())
```