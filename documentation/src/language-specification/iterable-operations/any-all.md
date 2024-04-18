# Any, All

Checking if any/all elements of an Iterable, CharSequence or array all matches a given predicates
is possible using the below syntax.

## Any
```marcel
list<int> list = [1, 2, 3, 4]
println(when int a in list |> a >= 3) // true
```

The `|>` arrow is used to check if at least one element matches the predicate.

## Any
```marcel
list<int> list = [1, 2, 3, 4]
println(when int a in list &> a >= 3) // false
```

The `&>` arrow is used to check if all elements matche the predicate.

## Negations
You can also negate those conditions using the `!when` keyword.

```marcel
println(!when int a in list |> a >= 3) // false
println(!when int a in list &> a >= 3) // true
```

## Complex boolean expressions
To use properly the above described operations in boolean expressions, wrap them with the parenthesis to
avoid any ambiguity

```marcel
if ((when int a in list &> a >= 3) && somethingElse) {
  doAllTheThings()
}
```
