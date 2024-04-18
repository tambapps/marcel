# Map and/or Filter

## Map
You can create a new collection resulting of the mapping of another.

```marcel
list<int> list = [1, 2, 3, 4]
list<int> list2 = [for int i in list -> i + 1] // [2, 3, 4, 5]
```

## Filter
Filtering is also possible using a similar syntax.

```marcel
list<int> list = [1, 2, 3, 4]
list<int> list2 = [for int i in list if i <= 2] // [1, 2]
```

## Map and Filter
You can do both in one operation.

```marcel
list<int> list = [1, 2, 3, 4]
list<float> list2 = [for int i in list -> i + 0.1f if i <= 2] // [1.1f, 2.2f]
```

## Casting
All the above operations can return Lists, primitive Lists, Sets, or primitive Sets. The type is
usually guessed by the compiler when possible (e.g. looking at the type of the variable you're trying to set), but
you can explicitly specify the wanted type using the [as operator](../operators/as.md).

```marcel
list<int> list = [1, 2, 3, 4]
set<int> list2 = [for int i in list -> i % 2] // [0, 1]
println([for int i in list -> Optional.of(i % 2)] as Set) // [Optional(0), Optional(1)]
```