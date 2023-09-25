# Ranges

A Range represents the list of discrete items between some starting (or from) value and working towards some ending (or to) value.
It may be reversed (e.g. from 10 to 1).

Marcel provides `IntRange` and `LongRange`



```marcel
for (int i in 0..<5) println(i)

for (long i in 10l..1l) println(i)

```


You can create int (and long) ranges

```marcel
0..10 // 0 (inclusive) to 10 (inclusive)
0<..10 // 0 (exclusive) to 10 (inclusive)
0..<10 // 0 (inclusive) to 10 (exclusive)
0<..<10 // 0 (exclusive) to 10 (exclusive)
```

Ranges also work in reverse order

```marcel
10..0 // 10 (inclusive) to 0 (inclusive)
10>..0 // 10 (exclusive) to 0 (inclusive)
10..>0 // 10 (inclusive) to 0 (exclusive)
10>..>0 // 10 (exclusive) to 0 (exclusive)
```

Ranges work with all kinds of int/long expressions

```marcel
int start = computeStart()
int end = computeEnd()

for (int i in start..(end - 1)) println(i)
```