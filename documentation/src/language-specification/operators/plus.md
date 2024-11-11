# Plus operator (+)

The plus operator is used for arithmetic and concatenating Strings, but in Marcel it also has other uses.

## Add collections

You can use `+` to add Collections. The two operand will not be modified. A new collection will be created with both operand
added to it.

It works well with lists

```marcel

List<int> myList1 = [1, 2, 3]
List<int> myList2 = [4, 5, 6]

List<int> myList3 = mySet1 + mySet2
println(myList3) // [1, 2, 3, 4, 5, 6]
```

and sets

```marcel

Set<int> mySet1 = [1, 2, 3]
Set<int> mySet2 = [3, 4, 5]

Set<int> mySetUnion = mySet1 + mySet2
mySetUnion(myList3) // [1, 2, 3, 4, 5]
```

<br/>

You can also add different kind of collections. The type of the returned collection will be the same as the first operand

```marcel
Set<int> newSet = mySet1 + myList1
List<int> newList = myList1 + mySet1
```
