package marcel.csv;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.exceptions.CsvValidationException;
import marcel.lang.compile.BooleanDefaultValue;
import marcel.lang.compile.NullDefaultValue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DynamicCsv {

  private DynamicCsv() {
    throw new IllegalStateException("You can't touch this");
  }

  public static CsvReader reader(String text,
                          @NullDefaultValue Character separator,
                          @NullDefaultValue Character quoteChar,
                          @NullDefaultValue Character escapeChar,
                          @NullDefaultValue Boolean withHeader,
                          @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new StringReader(text), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public static CsvReader reader(byte[] bytes,
                          @NullDefaultValue Character separator,
                          @NullDefaultValue Character quoteChar,
                          @NullDefaultValue Character escapeChar,
                          @NullDefaultValue Boolean withHeader,
                          @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new ByteArrayInputStream(bytes), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public static CsvReader reader(InputStream inputStream,
                          @NullDefaultValue Character separator,
                          @NullDefaultValue Character quoteChar,
                          @NullDefaultValue Character escapeChar,
                          @NullDefaultValue Boolean withHeader,
                          @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public static CsvReader reader(File file,
                          @NullDefaultValue Character separator,
                          @NullDefaultValue Character quoteChar,
                          @NullDefaultValue Character escapeChar,
                          @NullDefaultValue Boolean withHeader,
                          @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new FileReader(file), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public static CsvReader fileReader(String path,
                              @NullDefaultValue Character separator,
                              @NullDefaultValue Character quoteChar,
                              @NullDefaultValue Character escapeChar,
                              @NullDefaultValue Boolean withHeader,
                              @NullDefaultValue Boolean strictQuotes) throws IOException, CsvValidationException {
    return reader(new File(path), separator, quoteChar, escapeChar, withHeader, strictQuotes);
  }

  public static CsvReader reader(Reader reader,
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

  public static CsvWriter writer(OutputStream outputStream,
                                 @NullDefaultValue Character separator,
                                 @NullDefaultValue Character quoteChar,
                                 @NullDefaultValue Character escapeChar,
                                 @BooleanDefaultValue boolean applyQuotesToAll) {
    return writer(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), separator, quoteChar, escapeChar, applyQuotesToAll);
  }

  public static CsvWriter writer(File file,
                                 @NullDefaultValue Character separator,
                                 @NullDefaultValue Character quoteChar,
                                 @NullDefaultValue Character escapeChar,
                                 @BooleanDefaultValue boolean applyQuotesToAll) throws IOException {
    return writer(new FileWriter(file), separator, quoteChar, escapeChar, applyQuotesToAll);
  }

  public static CsvWriter fileWriter(String path,
                                     @NullDefaultValue Character separator,
                                     @NullDefaultValue Character quoteChar,
                                     @NullDefaultValue Character escapeChar,
                                     @BooleanDefaultValue boolean applyQuotesToAll) throws IOException {
    return writer(new FileWriter(path), separator, quoteChar, escapeChar, applyQuotesToAll);
  }

  public static CsvWriter writer(Writer writer,
                                 @NullDefaultValue Character separator,
                                 @NullDefaultValue Character quoteChar,
                                 @NullDefaultValue Character escapeChar,
                                 @BooleanDefaultValue boolean applyQuotesToAll) {
    CSVWriterBuilder writerBuilder = new CSVWriterBuilder(writer);
    if (separator != null) {
      writerBuilder.withSeparator(separator);
    }
    if (quoteChar != null) {
      writerBuilder.withQuoteChar(quoteChar);
    }
    if (escapeChar != null) {
      writerBuilder.withEscapeChar(escapeChar);
    }
    return new CsvWriter(writerBuilder.build(), applyQuotesToAll);
  }
}
