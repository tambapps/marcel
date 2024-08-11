# Comparison Operators

## Equal (==)

Unlike in Java `==` operator will do its best to compare the 2 operands by value. This operator is null safe, meaning that if one of the operand is null, it won't throw a `NullPointerException`.

This operator works as described below
- if both operand are primitives, Java-like `==` will be performed
- if both operand are primitive arrays, Arrays.equals() will be used to compare the values
- if both operand are object arrays, Objects.deepEquals() will be used to compare the values
- otherwise it will call Objects.equals()

This logic is applied at compile-time, meaning you won't have a runtime overhead because the program would have to check types.
- if at least one of the two operand is an Object, the other operand is casted as an object if needed and the Marcel `==` is applied.

## Not Equal (!=)
The not equal is the negation of the Marcel's `==`

## LT, LOE, GT, GOE (<, <= ,>, >=)

These operators works like in Java for primitive types. For object types, Marcel will check at compile-time if the first operand
has a `compareTo()` method and apply it on the second operand. The result of the compareTo will be used to apply the given comparison.

## Is Same (===)

This operator is the Java's `==` operator for Objects. It will check if the two operand are the same instance, or are both null.
(Note that it can't be used on primitives).

## Is Not Same (!==)
It is the negation of the Is Same operator.