# Minus operator (-)

The minus operator is used for arithmetic but in Marcel it also has other uses.

## Add collections

You can use `-` to add Collections. The two operand will not be modified. A new collection will be created with the elements 
of the first operand having removed all elements from the second operand if any.

It works well with lists

```marcel

List<int> myList1 = [1, 2, 3]
List<int> myList2 = [3, 4, 5]

List<int> myList3 = mySet1 - mySet2
println(myList3) // [1, 2]
```

and sets

```marcel

Set<int> mySet1 = [1, 2, 3]
Set<int> mySet2 = [3, 4, 5]

Set<int> mySetUnion = mySet1 - mySet2
println(mySetUnion) // [1, 2]
```

<br/>

You can also add different kind of collections. The type of the returned collection will be the same as the first operand

```marcel
Set<int> newSet = mySet1 - myList1
List<int> newList = myList1 - mySet1
```
