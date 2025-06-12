# Parsing and producing CSV

Marcel comes with integrated support for converting between Marcel objects and CSV.
The classes dedicated to CSV parsing are found in the `marcel.csv` package.

The CSV (de)serialization is backed by [OpenCSV](https://opencsv.sourceforge.net/). All is handled by the `DynamicCsv` class.

## Parsing CSV

The parsing of CSV takes advantage of [Dynamic Objects](../language-specification/types/dynamic-objects.md) to make the API easy to use.

Here is an example:
```marcel
import marcel.csv.*

List companies;
// with headers to parse the first line of the CSV as the field names
try (CsvReader reader = DynamicCsv.reader(new File('companies.csv'), withHeader: true)) {
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
try (CsvReader reader = DynamicCsv.reader(new File('companies.csv'))) {
  while (dynobj line = reader.readNext()) {
    if (line[2].asString().split(',').find { it == 'Java' }) {
      companies << line[0]
    }
  }
}
println(companies)
```

There are many optional parameters to the `reader()` methods:
- separator: the separator character of the CSV (defaults to `,`)
- quoteChar: the quote character of the CSV (defaults to `"`)
- escapeChar: the escape character (defaults to `\`)
- withHeader: whether the first line should be considered as an header and parsed automatically (defaults to `false`)
- strictQuotes: whether to ignore characters outside the quotes (defaults to `false`)


Here is the CSV used for the above examples
```csv
companyName,website,keywords,nbEmployees
Company A,https://company.a.fr,"Java, Spring Boot",10
Company B,https://company.b.fr,"Javascript",5000
Company C,https://company.c.fr,"Java, Quarkus",250
Company D,https://company.d.fr,"Python",43
```

## Producing CSV

Writing a CSV is pretty much straight-forward

```marcel
import marcel.csv.*
List companies;

try (CsvWriter writer = DynamicCsv.writer(new File('output.csv'))) {
  writer.write(['companyName','website','keywords','nbEmployees'])
  writer.write(List.of("company A", "http://company.a/", 5))
  writer.write(DynamicObject.of(["Company B","https://company.b.fr","Javascript",5000]))
  writer.write(["company, C", "http://company.c/", IntRanges.of(1, 10)])
}
```

There are many optional parameters to the `writer()` methods:
- separator: the separator character of the CSV (defaults to `,`)
- quoteChar: the quote character of the CSV (defaults to `"`)
- escapeChar: the escape character (defaults to `\`)
- applyQuotesToAll: whether all cells should be quoted even when not necessary (defaults to `false`)
