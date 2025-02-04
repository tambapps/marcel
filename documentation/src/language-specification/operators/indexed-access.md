# Indexed Access Operator (expr[index])

This operator is usually (or at least in Java) reserved for arrays, 
but in Marcel you can also use it to access lists. You can get/set 
items of your list using the same syntax as for an array

```marcel

List<int> list = [1, 2, 3]

println(list[0])

list[1] = 4
```

<br/>

You can also [define your own accesses for custom types](./operator-overloading.md)

## Safe indexed access (getAtSafe)

Similarly to [safe navigation](./safe-navigation.md), you can access elements of list/arrays

```marcel
println(list?[5]) // will print null
```

This operator checks that the index provided is within the list/array's bounds (`0 <= index < length`)

You can also set elements safely with the `putAtSafe` operator

```marcel
List<int> = [1, 2, 3]

list?[1] = 5 // will actually set the value
list?[10] = 4 // will not set the value as the index is not within bounds
```