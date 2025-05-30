package marcel.csv;

import com.opencsv.CSVReader;
import lombok.SneakyThrows;
import marcel.lang.DynamicObject;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

class CsvLineIterator implements Iterator<DynamicObject> {

  private final CSVReader csvReader;
  // nullable
  private final List<String> headers;

  private DynamicCsvLine next;

  CsvLineIterator(CSVReader csvReader, List<String> headers) {
    this.csvReader = csvReader;
    this.headers = headers;
    readNext();
  }

  @Override
  public boolean hasNext() {
    return this.next != null;
  }

  @Override
  public DynamicCsvLine next() {
    if (this.next == null) {
      throw new NoSuchElementException();
    }
    DynamicCsvLine current = this.next;
    readNext();
    return current;
  }

  @SneakyThrows
  private void readNext() {
    String[] columns = csvReader.readNext();
    this.next = columns != null ? new DynamicCsvLine(headers, List.of(columns)) : null;
  }
}
