# Types

## Primitives
Marcel supports the following Java primitives

- void
- boolean
- byte
- int
- long
- float
- double

To see how to create such types, go through the [Literal Values section](./syntax/literal-values.md)

## Classes

Marcel is a JVM language, therefore you can use any classes defined in the JDK

## Collections of Primitives

Marcel allows to use collections with primitive elements. Such collections will not box all your primitives into their 
related Object class (e.g. store an int into an Integer). The elements will be stored in an array of primitives.

Iterating over such collections will only use primitives, no (un)boxing will be done.

Let's learn by example

## Lists
````groovy
list<int> list = [1, 2, 3, 4]

println(list[1])

list[1] = 1
println(list[1])
````

Here, we're declaring a `list<int>`. This type isn't actually generic, it is actually an IntList (you can see this class in the marcel stdlib), and the
literal array will be converted into a IntArrayList (a int list that store elements in an int array).


Here is the list of all list of primitives supported
- list<int> -> IntList
- list<long> -> LongList
- list<char> -> CharacterList
- list<float> -> FloatList
- list<double> -> DoubleList

## Sets
You can do the same with sets

````groovy
set<int> mySet = [1, 2, 3, 3] // will actually contain just 1, 2 and 3
````

Here is the list of all set of primitives supported
- set<int> -> IntSet
- set<long> -> LongSet
- set<char> -> CharacterSet

## Maps of Primitive keys
You can also use maps using primitive keys (but all values will be Objects, meaning that your ints will be boxed into Integers)

````groovy
map<int, Integer> myMap = [1: 1, 2: 2, 3: 3] // will actually contain just 1, 2 and 3
````

Here is the list of all map of primitive keys supported
- map<int, ?> -> Int2ObjectMap
- map<long, ?> -> Long2ObjectMap
- map<char, ?> -> Character2ObjectMap