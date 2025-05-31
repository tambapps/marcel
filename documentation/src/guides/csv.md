# Parsing CSV

Marcel comes with integrated support for converting between Marcel objects and CSV.
The classes dedicated to CSV parsing are found in the `marcel.csv` package.

The CSV deserialization is backed by [Jackson](https://opencsv.sourceforge.net/).

All is handled by the `DynamicCsv` class. It takes
advantage of [Dynamic Objects](../language-specification/types/dynamic-objects.md) to make this API easy to use.

Here is an example:
```marcel
List companies;
// with headers to parse the first line of the CSV as the field names
try (CsvReader reader = DynamicCsv.instance.withHeader()
    .reader(new File('dlcsv.csv'))) {
  companies = [for dynobj line in reader -> line.companyName if line.keywords.find { it == 'Java' } ]
}
println(companies)
```

You can also access a line's values by index:
```marcel
List companies = []
try (CsvReader reader = DynamicCsv.instance.reader(new File('dlcsv.csv'))) {
    while (dynobj line = reader.readNext()) {
      if (line[7].find { it == 'Java' }) {
        companies << line[0]
      }
    }
}
println(companies)
```