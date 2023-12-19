# Metaprogramming in Marcel

Marcel offers some metaprogramming features, allowing you to write code that generates code.

Such code is generating while compiling it. As Marcel is a static language, it only supports compile-time metaprogramming
and has **no** [runtime metaprogramming features](https://groovy-lang.org/metaprogramming.html#_runtime_metaprogramming).

## Compile-time metaprogramming with AST transformations

This process alters the [Abstract Syntax Tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree) (AST) of your program.

Marcel has a concept of AST transformations [similar as Groovy](https://groovy-lang.org/metaprogramming.html#_compile_time_metaprogramming).

These interfaces specify the transformation of an AST node and operates in 2 steps occurring in different phases of the compilation process.

### How AST Transformations operates

After the [parsing](https://en.wikipedia.org/wiki/Parsing) of your code, and before performing the [semantic analysis](https://en.wikipedia.org/wiki/Semantic_analysis_(compilers)), all symbols (classes, methods, fields) are defined.

Then occurs the first step of the AST transformation: the type transformation. An AST transformation can alter the definition of symbols (e.g. make your class implement an interface, or add a field to a class, modify a method signature...).

After the AST transformations completed their first step, the semantic analysis is performed. 

Finally, comes the second step of AST transformations: the actual AST transformation. This step can alter in many ways the AST
but **it must do it carefully as no further semantic checks will be done after**. 

The modifications made on the AST must also be coherent with the previous symbol transformations 
(e.g. if we defined a new method in the first step, we must create and add a new valid method node with the same signature in the AST, in the second step).
