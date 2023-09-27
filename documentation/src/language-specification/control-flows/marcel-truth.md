# Marcel truth

A truthy value is a value that is considered true for an if, or a while. 

A falsey value is a value that is considered false in those places.

The only falsey values are the following:
- `false` 
- `null`
- `Optional.empty()`
- An empty collection
- An empty array
- An empty Map
- A [Matcher](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html) who's `find()` method would return false
- A TruthableObject who's `isTruthy()` method would return false

Any other value is truthy.

You can override the truth for your class if you make it extend `TruthableObject`