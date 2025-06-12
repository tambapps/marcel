package marcel.csv;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import marcel.lang.compile.NullDefaultValue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

// TODO handle serialization, don't forget to complete mdbook doc
public class DynamicCsv {

  // useful to handle csv without having to instantiate a mapper
  private static DynamicCsv _instance = new DynamicCsv();

  public static DynamicCsv getInstance() {
    if (_instance == null) {
      _instance = new DynamicCsv();
    }
    return _instance;
  }

  public CsvReader reader(String text,
                          @NullDefaultValue Character separator,
                          @NullDefaultValue Character quoteChar,
                          @NullDefaultValue Character escapeChar,
                          @NullDefaultValue Boolean withHeader,
                          @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new StringReader(text), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public CsvReader reader(byte[] bytes,
                          @NullDefaultValue Character separator,
                          @NullDefaultValue Character quoteChar,
                          @NullDefaultValue Character escapeChar,
                          @NullDefaultValue Boolean withHeader,
                          @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new ByteArrayInputStream(bytes), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public CsvReader reader(InputStream inputStream,
                          @NullDefaultValue Character separator,
                          @NullDefaultValue Character quoteChar,
                          @NullDefaultValue Character escapeChar,
                          @NullDefaultValue Boolean withHeader,
                          @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public CsvReader reader(File file,
                          @NullDefaultValue Character separator,
                          @NullDefaultValue Character quoteChar,
                          @NullDefaultValue Character escapeChar,
                          @NullDefaultValue Boolean withHeader,
                          @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new FileReader(file), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public CsvReader fileReader(String path,
                              @NullDefaultValue Character separator,
                              @NullDefaultValue Character quoteChar,
                              @NullDefaultValue Character escapeChar,
                              @NullDefaultValue Boolean withHeader,
                              @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new File(path), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public CsvReader reader(Reader reader,
                          @NullDefaultValue Character separator,
                          @NullDefaultValue Character quoteChar,
                          @NullDefaultValue Character escapeChar,
                          @NullDefaultValue Boolean withHeader,
                          @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    CSVParserBuilder parserBuilder = new CSVParserBuilder();
    if (separator != null) {
      parserBuilder.withSeparator(separator);
    }
    if (quoteChar != null) {
      parserBuilder.withQuoteChar(quoteChar);
    }
    if (escapeChar != null) {
      parserBuilder.withEscapeChar(escapeChar);
    }
    if (strictQuotes != null) {
      parserBuilder.withStrictQuotes(strictQuotes);
    }
    CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parserBuilder.build()).build();
    List<String> headers = null;
    if (withHeader != null && withHeader) {
      headers = List.of(csvReader.readNext());
    }
    return new CsvReader(csvReader, headers);
  }
}
