# Plus operator (+)

The plus operator is used for arithmetic and concatenating Strings, but in Marcel it also has other uses.

## Add collections

You can use `+` to add Collections. The two operand will not be modified. A new collection will be created with both operand
added to it.

It works well with lists

```java

list<int> myList1 = [1, 2, 3]
list<int> myList2 = [4, 5, 6]

list<int> myList3 = mySet1 + mySet2
println(myList3) // [1, 2, 3, 4, 5, 6]
```

and sets

```java

set<int> mySet1 = [1, 2, 3]
set<int> mySet2 = [3, 4, 5]

set<int> mySetUnion = mySet1 + mySet2
mySetUnion(myList3) // [1, 2, 3, 4, 5]
```

<br/>

You can also add different kind of collections. The type of the returned collection will be the same as the first operand

```java
set<int> newSet = mySet1 + myList1
list<int> newList = myList1 + mySet1
```
