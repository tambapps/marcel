# Collections of Primitives

Marcel allows to use collections with primitive elements. Such collections will not box all your primitives into their
related Object class (e.g. store an int into an Integer). The elements will be stored in an array of primitives.

Iterating over such collections will only use primitives, no (un)boxing will be done.

Let's learn by example

## Lists
````marcel
List<int> list = [1, 2, 3, 4]

println(list[1])

list[1] = 1
println(list[1])
````

Here, we're declaring a `List<int>`. This type isn't actually generic, it is in fact an IntList (you can see this class in the marcel stdlib), and the
literal array will be converted into a IntArrayList (a int list that store elements in an int array).


Here is the list of all list of primitives supported
- List\<int> -> IntList
- List\<long> -> LongList
- List\<float> -> FloatList
- List\<double> -> DoubleList
- List\<char> -> CharacterList

## Sets
You can do the same with sets

````marcel
Set<int> mySet = [1, 2, 3, 3] // will actually contain just 1, 2 and 3
````

Here is the list of all set of primitives supported
- Set\<int> -> IntSet
- Set\<long> -> LongSet
- Set\<float> -> FloatSet
- Set\<double> -> DoubleSet
- Set\<char> -> CharacterSet
