# Regular Expressions (Pattern Matching)

Marcel's [Pattern strings](../language-specification/types/string.md#pattern-strings)
allows you to create pattern in a simple manner. When you add that with the find operator,
matching with regular expression has never been this easy.

````marcel
Pattern pattern = r/Hello (\w+)/; // semi-colon required because of pattern flags
Matcher matcher = "Hello you" =~ pattern
println(matcher.matches())
````

The above code tests is the String `Hello you` matches the pattern `Hello (\w+)`.

## Extract groups from a pattern
With Marcel's [multiple variable declaration](../language-specification/variables.md#multiple-declarations), you can extract
matched groups in the following way

````marcel
def (String wholeMatch, String groupMatch) = ("Hello you" =~ r/Hello (\w+)/).groups() // method from the Marcel Development Kit
````

In some case, you might not care about the whole match (you just want the groups you declared in your regex).
If that's so you can ignore it like this

````marcel
def (_, String groupMatch) = ("Hello you" =~ r/Hello (\w+)/).groups()
````

Wait for it, there's an even better way to do that
````marcel
def (String groupMatch) = ("Hello you" =~ r/Hello (\w+)/).definedGroups()
````

The defined groups only return the groups you defined in the regex, and therefore skip the group corresponding to the whole match.

## Truthy pattern declaration

```marcel
Pattern pattern = r/Hello (\w+)/;

if ("yellow me" =~ pattern) {
  println("It matched????")
}

if (Matcher m = "Hello me" =~ pattern) {
  println("It matched" + m.group(1))
}
```