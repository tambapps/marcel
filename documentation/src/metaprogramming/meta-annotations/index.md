# Meta-annotations (metaprogramming with annotations)
[AST transformations](../index.md) can be specified on annotations. Doing so allows to perform specific transformations when annotating a given
class, field, method and/or field.

Marcel's standard library provides many annotations useful to avoid writing boilerplate code. Some of them are similar as the one you could find in [Lombok](https://projectlombok.org/).

Meta-annotations from the Marcel's standard-library are all lowercase (even the first letter), this is how you can differentiate them from
other (non-meta) annotations.
