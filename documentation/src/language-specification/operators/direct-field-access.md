# Direct field access @fieldName

This operator allows to make sure to reference a class field, and not a getter/setter.

E.g.
```marcel
class Foo {
 int bar
  
  fun getBar() {
    return this.@bar
  }
  
  fun setBar(int bar) {
    this.@bar = bar
  }
}

Foo foo = new Foo()
```
In the above class, calling `foo.@bar` or `foo.@bar = 4` would make sure to actually use the field, and not the getter/setter.