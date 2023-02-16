# Visibilities

In marcel, there are 4 kinds of visibility.

- `public` -> which refers to Java's public visibility. Your class/method/field may be accessible from any package
- `protected` -> which refers to Java's protected visibility. Your class/method/field may only be accessible from other classes in the same package or inheriting your class
- `internal` -> which refers to Java's package-private visibility. Your class/method/field may only be accessible from classes in the same package
- `private` -> Your method may be accessible only from the class it was defined in

The default visibility is `public` (meaning that when it isn't specified, the class/method/field will be considered as public)