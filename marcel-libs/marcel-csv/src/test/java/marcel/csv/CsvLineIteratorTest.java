package marcel.csv;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CsvLineIteratorTest {

  private final CSVReader reader = mock(CSVReader.class);

  @Test
  public void testEmpty() throws CsvValidationException, IOException {
    when(reader.readNext()).thenReturn(null);

    CsvLineIterator iterator = new CsvLineIterator(reader, null);
    assertFinished(iterator);
  }

  @Test
  public void testNext() throws CsvValidationException, IOException {
    when(reader.readNext()).thenReturn(
        new String[] { "a", "b", "c" },
        new String[] { "1", "2", "3" },
        null
    );

    CsvLineIterator iterator = new CsvLineIterator(reader, null);
    assertEquals(new DynamicCsvLine(List.of("a", "b", "c")), iterator.next());
    assertEquals(new DynamicCsvLine(List.of("1", "2", "3")), iterator.next());
    assertFinished(iterator);
  }

  private void assertFinished(CsvLineIterator iterator) {
    assertFalse(iterator.hasNext());
    assertThrows(NoSuchElementException.class, iterator::next);
  }
}
