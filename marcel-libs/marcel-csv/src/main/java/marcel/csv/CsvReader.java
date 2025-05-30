package marcel.csv;

import com.opencsv.CSVReader;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import marcel.lang.DynamicObject;
import marcel.lang.MarcelTruth;
import marcel.lang.dynamic.DynamicList;
import marcel.lang.lambda.DynamicObjectLambda1;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * CSV reader
 */
@AllArgsConstructor
public class CsvReader implements Closeable, Iterable<DynamicObject> {

  private final CSVReader csvReader;
  // nullable
  private final List<String> headers;

  @Override
  @SneakyThrows
  public Iterator<DynamicObject> iterator() {
    return new CsvLineIterator(csvReader, headers);
  }

  @SneakyThrows
  public DynamicObject findLines(DynamicObjectLambda1 lambda1) {
    List<DynamicCsvLine> lines = new ArrayList<>();
    String[] values;
    while ((values = csvReader.readNext()) != null) {
      DynamicCsvLine line = new DynamicCsvLine(headers, List.of(values));
      if (MarcelTruth.isTruthy(lambda1.invoke(line))) {
        lines.add(line);
      }
    }
    return new DynamicList(lines);
  }

  @SneakyThrows
  public DynamicObject findLine(DynamicObjectLambda1 lambda1) {
    String[] values;
    while ((values = csvReader.readNext()) != null) {
      DynamicCsvLine line = new DynamicCsvLine(headers, List.of(values));
      if (MarcelTruth.isTruthy(lambda1.invoke(line))) {
        return line;
      }
    }
    return null;
  }

  @SneakyThrows
  public DynamicObject mapLines(DynamicObjectLambda1 lambda1) {
    List<DynamicObject> lines = new ArrayList<>();
    String[] values;
    while ((values = csvReader.readNext()) != null) {
      lines.add(lambda1.invoke(new DynamicCsvLine(headers, List.of(values))));
    }
    return new DynamicList(lines);
  }

  @SneakyThrows
  public DynamicObject readNext() {
    String[] values = csvReader.readNext();
    if (values == null) {
      return null;
    }
    return new DynamicCsvLine(headers, List.of(values));
  }

  @SneakyThrows
  public List readAll() {
    List<DynamicCsvLine> lines = new ArrayList<>();
    String[] values;
    while ((values = csvReader.readNext()) != null) {
      DynamicCsvLine line = new DynamicCsvLine(headers, List.of(values));
      lines.add(line);
    }
    return lines;
  }

  @Override
  @SneakyThrows
  public void forEach(Consumer<? super DynamicObject> action) {
    String[] values;
    while ((values = csvReader.readNext()) != null) {
      action.accept(new DynamicCsvLine(headers, List.of(values)));
    }
  }

  @Override
  public void close() throws IOException {
    csvReader.close();
  }
}
