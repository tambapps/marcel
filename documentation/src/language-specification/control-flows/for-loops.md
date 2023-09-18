# For loops

There are different ways to iterate over elements

#### For i
The Java for i is compatible with Marcel
```marcel
for (int i = 0; i < 10; i++) {
  println(i)
}
```

#### For in
The `in` keyword allows to iterate over values in an array, any objects implementing Iterable (including all Collections) or Iterator.
```marcel
int[] ints = getInts()
for (int i in ints) {
  println(i)
}
```
Marcel also have a Ranges, allowing you to iterate with the below syntax

```marcel
// inclusive range
for (int i in 0..9) {
  println(i)
}

// exclusive range
for (int i in 0..<10) {
  println(i)
}

// also work in reverse orde
for (int i in 9..0) {
  println(i)
}

// exclusive range
for (int i in 10>..0) {
  println(i)
}
```
