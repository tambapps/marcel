package marcel.csv;

import com.opencsv.ICSVWriter;
import lombok.AllArgsConstructor;
import marcel.lang.DynamicObject;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class CsvWriter implements Closeable {

  private final ICSVWriter csvWriter;
  private final boolean applyQuotesToAll;

  public void write(Object[] data) {
    write(Arrays.stream(data).map(Objects::toString).toArray(String[]::new));
  }

  public void write(DynamicObject object) {
    write(object.asList());
  }

  public void write(List<?> data) {
    write(data.stream().map(Objects::toString).toArray(String[]::new));
  }

  public void write(String[] data) {
    csvWriter.writeNext(data, applyQuotesToAll);
  }

  @Override
  public void close() throws IOException {
    csvWriter.close();
  }
}
