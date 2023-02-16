# Marcel truth

A truthy value is a value that is considered true for an if, or a while. 

A falsey value is a value that is considered false in those places.

The only falsey values are the following:
- `null`
- `Optional.empty()`
- An empty collection
- An empty array

Any other value is truthy.

You can also override the truth for your class if you define a function `fun isTruthy() bool`