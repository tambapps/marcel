# Comparison Operators

## Equal (==)

Unlike in Java `==` operator refers to the Object.equals() method (for objects comparison), and some custom comparison added (e.g. for collections).
This operator is null safe, meaning that if one of the operand is null, it won't throw a `NullPointerException`.

This operator works as described below
- if both operand are primitives, Java-like `==` is performed
- if at least one of the two operand is an Object, the other operand is casted as an object if needed and the Marcel `==` is applied.


The Marcel `==` works with the following rules

- if the first operand **is** the second operand (same instance), it returns true
- if the left or the right operand is `null`, return false
- if the two operand are arrays, return true if arrays content are the same
- returns `operand1.equals(operand2)`

## Not Equal (!=)
The not equal apply is the negation of the Marcel's `==`

## LT, LOE, GT, GOE (<, <= ,>, >=)

These operators works like in Java for primitive types. For object types, Marcel will check at compile-time if the first operand
has a `compareTo()` method and apply it on the second operand. The result of the compareTo will be used to apply the given comparison.

## Is Same Instance (===)

This operator is the Java's `==` operator for Objects. It will check if the two operand are the same instance.
(Note that it can't be used on primitives).

## Is Not Same Instance (!==)
It is the negation of the Is Same Instance operator.