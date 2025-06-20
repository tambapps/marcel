# Parsing and producing YAML

Marcel comes with integrated support for converting between Marcel objects and YAML.
The classes dedicated to YAML serialization and parsing are found in the `marcel.yaml` package.

The YAML (de)serialization is backed by [Jackson](https://github.com/FasterXML/jackson).

All is handled by the `DynamicYaml` class. It takes
advantage of [Dynamic Objects](../language-specification/types/dynamic-objects.md) to make this API easy to use.



```marcel
import marcel.yaml.*


dynobj yaml = DynamicYaml.instance.read('{"id": 0, "name": "Something", "tags": ["fun", "adventure", "friendship"], "metadata": {"foo": "bar"}}')
println(yaml['name'] == yaml.name) // true
println(yaml.tags[2]) // friendship
println(yaml.metadata.foo) // bar
println(yaml.id.asInt() + 1) // 1

yaml.name = "Something Else"

println(DynamicYaml.instance.writeAsString(yaml))
DynamicYaml.instance.write([yaml], new File('output.yaml')) // write yaml list in file
```