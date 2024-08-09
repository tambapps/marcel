# As (smart casting)

The `as` keyword allows to **smart** cast variables to a provided type.

## Smart casting

Smart casting is like an enhanced Java cast. It can cast objects like a Java cast would, but it
can also convert the object to make it fit the target type.

### Collections smart casting

The smart cast can transform arrays into (primitive) lists/sets 


```marcel
int[] array = [1, 2, 3]

list<int> intList = array as list<int>
set<long> longSet = [1l, 2l, 3l] as set<long>
```
### Boolean (truthy) smart casting
You can smart cast any value to a boolean. The value of the boolean will be determined based
on the [Marcel truth](../control-flows/marcel-truth.md).

### Dynamic object smart casting
Any type can become a dynamic object

```marcel
dynobj obj = 1 as dynobj
```

## (Java) casting

To perform a simple cast, you can use the function `cast` with the [diamond operator](../source-file-structure/functions.md#cast-results), 
provided specifically for this use-case

```marcel
int a = 1

Integer b = a as Integer
Number c = b as Number
Long d = c as Long // will fail as a is not an instance of Long
```