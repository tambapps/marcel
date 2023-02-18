# Visibility and Access

In marcel, there are 4 kinds of visibility.

- `public` -> which refers to Java's public visibility. Your class/method/field may be accessible from any package
- `protected` -> which refers to Java's protected visibility. Your class/method/field may only be accessible from other classes in the same package or inheriting your class
- `internal` -> which refers to Java's package-private visibility. Your class/method/field may only be accessible from classes in the same package
- `private` -> Your method may be accessible only from the class it was defined in

The default visibility is `public` (meaning that when it isn't specified, the class/method/field will be considered as public)

## Access

Class/method/fields access should be specified in the below order.

1. public/protected/internal/private (or nothing, which would default to public visibility)
2. static (Optional. only if you want your member to be static)
3. final (Optional. only if you want your member to be final)
