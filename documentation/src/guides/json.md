# Parsing and producing JSON

Marcel comes with integrated support for converting between Marcel objects and JSON. 
The classes dedicated to JSON serialization and parsing are found in the `marcel.json` package.

The JSON (de)serialization is backed by [Jackson](https://github.com/FasterXML/jackson).

All is handled by the `DynamicJson` class. It takes
advantage of [Dynamic Objects](../language-specification/types/dynamic-objects.md) to make this API easy to use.


```marcel
import marcel.json.*


dynobj json = DynamicJson.instance.read('{"id": 0, "name": "Something", "tags": ["fun", "adventure", "friendship"], "metadata": {"foo": "bar"}}')
println(json['name'] == json.name) // true
println(json.tags[2]) // friendship
println(json.metadata.foo) // bar
println(json.id.asInt() + 1) // 1

json.name = "Something Else"

println(DynamicJson.instance.writeAsString(json)) // {"metadata":{"foo":"bar"},"name":"Something Else","id":0,"tags":["fun","adventure","friendship"]}
// use writeAsPrettyString to write a pretty indented JSON string

DynamicJson.instance.write([json], new File('output.json')) // write json list in file
```