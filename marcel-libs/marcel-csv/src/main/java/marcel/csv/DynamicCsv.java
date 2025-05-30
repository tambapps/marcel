package marcel.csv;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

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

public class DynamicCsv {

  private final CSVParserBuilder parserBuilder = new CSVParserBuilder();
  private boolean withHeader = false;

  // useful to handle csv without having to instantiate a mapper
  private static DynamicCsv _instance = new DynamicCsv();

  public static DynamicCsv getInstance() {
    if (_instance == null) {
      _instance = new DynamicCsv();
    }
    return _instance;
  }

  public DynamicCsv withSeparator(char c) {
    parserBuilder.withSeparator(c);
    return this;
  }

  public DynamicCsv withHeader() {
    return withHeader(true);
  }

  public DynamicCsv withoutHeader() {
    return withHeader(false);
  }

  public DynamicCsv withHeader(boolean withHeader) {
    this.withHeader = withHeader;
    return this;
  }

  public DynamicCsv withQuoteChar(char quoteChar) {
    parserBuilder.withQuoteChar(quoteChar);
    return this;
  }

  public DynamicCsv withEscapeChar(char escapeChar) {
    parserBuilder.withEscapeChar(escapeChar);
    return this;
  }

  public DynamicCsv withStrictQuotes(boolean strictQuotes) {
    parserBuilder.withStrictQuotes(strictQuotes);
    return this;
  }

  // TODO document this, and Yaml, and Json, and that the caller of those functions should call try with resources
  public CsvReader reader(String text) throws IOException, CsvValidationException {
    return reader(new StringReader(text));
  }

  public CsvReader reader(byte[] bytes) throws IOException, CsvValidationException {
    return reader(new ByteArrayInputStream(bytes));
  }

  public CsvReader reader(InputStream inputStream) throws IOException, CsvValidationException {
    return reader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
  }

  public CsvReader reader(File file) throws IOException, CsvValidationException {
    return reader(new FileReader(file));
  }

  public CsvReader reader(Reader reader) throws IOException, CsvValidationException {
    CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(parserBuilder.build()).build();
    List<String> headers = null;
    if (withHeader) {
      headers = List.of(csvReader.readNext());
    }
    return new CsvReader(csvReader, headers);
  }

  public CsvReader readFile(String path) throws IOException, CsvValidationException {
    return reader(new File(path));
  }
}
