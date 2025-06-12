# Parsing and producing CSV

Marcel comes with integrated support for converting between Marcel objects and CSV.
The classes dedicated to CSV parsing are found in the `marcel.csv` package.

## Parsing CSV

The CSV deserialization is backed by [Jackson](https://opencsv.sourceforge.net/).

All is handled by the `DynamicCsv` class. It takes
advantage of [Dynamic Objects](../language-specification/types/dynamic-objects.md) to make this API easy to use.

Here is an example:
```marcel
import marcel.csv.*
List companies;
// with headers to parse the first line of the CSV as the field names
try (CsvReader reader = DynamicCsv.instance.reader(new File('companies.csv'), withHeader: true)) {
  companies = [
    for dynobj line in reader -> line.companyName 
      if line.keywords.asString().split(',').find { it == 'Java' } 
  ]
}
println(companies)
```

You can also access a line's values by index:
```marcel
import marcel.csv.*
List companies = []
try (CsvReader reader = DynamicCsv.instance.reader(new File('companies.csv'))) {
    while (dynobj line = reader.readNext()) {
      if (line[2].asString().split(',').find { it == 'Java' }) {
        companies << line[0]
      }
    }
}
println(companies)
```


Here is the CSV used for the above examples

```csv
companyName,website,keywords,nbEmployees
Company A,https://company.a.fr,"Java, Spring Boot",10
Company B,https://company.b.fr,"Javascript",5000
Company C,https://company.c.fr,"Java, Quarkus",250
Company D,https://company.d.fr,"Python",43
```

## Producing CSV

TODO