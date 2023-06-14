# Minus operator (-)

The minus operator is used for arithmetic but in Marcel it also has other uses.

## Add collections

You can use `-` to add Collections. The two operand will not be modified. A new collection will be created with the elements 
of the first operand having removed all elements from the second operand if any.

It works well with lists

```groovy

list<int> myList1 = [1, 2, 3]
list<int> myList2 = [3, 4, 5]

list<int> myList3 = mySet1 - mySet2
println(myList3) // [1, 2]
```

and sets

```groovy

set<int> mySet1 = [1, 2, 3]
set<int> mySet2 = [3, 4, 5]

set<int> mySetUnion = mySet1 - mySet2
println(mySetUnion) // [1, 2]
```

<br/>

You can also add different kind of collections. The type of the returned collection will be the same as the first operand

```groovy
set<int> newSet = mySet1 - myList1
list<int> newList = myList1 - mySet1
```
