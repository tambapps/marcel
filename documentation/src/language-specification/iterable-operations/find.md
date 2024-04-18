# Find

This iterable operation allows to find an element on an Iterable, CharSequence or array and return it, or null.
As the element may not be found, this operator always return an object, even for primitive collections (e.g. Integer for a `list<int>`).

```marcel
list<int> list = [1, 2, 3, 4]
println(when int a in list -> a % 2 == 0) // 2
println(when int a in list -> a % 2 == 5) // null
```