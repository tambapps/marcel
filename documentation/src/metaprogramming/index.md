# Metaprogramming in Marcel

Marcel offers some metaprogramming features, allowing you to write code that generates code.

Such code is generating while compiling it. As Marcel is a static language, it only supports compile-time metaprogramming
and has **no** [runtime metaprogramming features](https://groovy-lang.org/metaprogramming.html#_runtime_metaprogramming).

## Compile-time metaprogramming with Syntax Tree Transformations

Syntax Tree Transformations can modify the representation of your source code before converting it into Java bytecode.

This process can alter the [Concrete Syntax Tree](https://en.wikipedia.org/wiki/Parse_tree) (CST) and/or the [Abstract Syntax Tree](https://en.wikipedia.org/wiki/Abstract_syntax_tree) (AST) of your program.

This concept is [similar as Groovy's AST transformations](https://groovy-lang.org/metaprogramming.html#_compile_time_metaprogramming), except that in Marcel
you can also transform the CST, and the AST transformation occurs **after** the [semantic analysis](https://en.wikipedia.org/wiki/Semantic_analysis_(compilers)). 

This `SyntaxTreeTransformation` interface specifies the transformation of a CST/AST node and operates in 2 steps occurring in different phases of the compilation process (3 if you count the initialization).

### How Syntax Tree Transformations operate

After the [parsing](https://en.wikipedia.org/wiki/Parsing) of your code which outputs the Concrete Syntax Tree (CST), and before performing the [semantic analysis](https://en.wikipedia.org/wiki/Semantic_analysis_(compilers)) which would generate the Abstract Syntax Tree (AST), all symbols (classes, methods, fields) are defined.

Then occurs the first step of an Syntax Tree transformation: the symbol definition transformation. A Syntax Tree transformation can alter the definition of symbols 
(e.g. make your class implement an interface, or add a field to a class, modify a method signature...). This step only affects the definition of the symbols. It tells the compiler
(for example) that "This class also implements the Foo interface" or "This class has a field bar of type int", and the compiler will just have to trust it, especially when performing the semantic analysis.

In this step you can also modify the Concrete Syntax Tree.

After all the Syntax Tree transformations completed their first step, the semantic analysis is performed. 

Finally, comes the second step of Syntax Tree transformations: the AST transformation. This step can alter in many ways the AST,
but **it must alter it carefully as not all semantic checks are performed while doing so**. 

The modifications made on the AST must also be coherent with the previous symbol definition transformations. 
E.g. if we defined a new method in the first step, we must create and add a new valid method node with the same signature in the AST, in the second step.
