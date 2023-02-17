# Indexed Access Operator (expr[index])

This operator is usually (or at least in Java) reserved for arrays, 
but in Marcel you can also use it to access lists. You can get/set 
items of your list using the same syntax as for an array

```groovy

list<int> list = [1, 2, 3]

println(list[0])

list[1] = 4
```

<br/>

You can also [define your own accesses for custom types](./operator-overloading.md)

## Safe indexed access

Similarly to [safe navigation](./safe-navigation.md), you can access elements of list/arrays

```groovy
println(list?[5]) // will print null
```

This operator checks that the index provided is within range (`0 <= index < length`)