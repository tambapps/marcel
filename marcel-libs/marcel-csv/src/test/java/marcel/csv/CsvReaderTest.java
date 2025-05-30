package marcel.csv;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import marcel.lang.DynamicObject;
import marcel.lang.methods.DefaultMarcelMethods;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CsvReaderTest {

  private final CSVReader csvReader = mock(CSVReader.class);

  @Test
  public void testRead() throws CsvValidationException, IOException {
    mockReader();

    CsvReader reader = new CsvReader(csvReader, null);
    assertEquals(new DynamicCsvLine(List.of("a", "b", "c")), reader.readNext());
    assertEquals(new DynamicCsvLine(List.of("1", "2", "3")), reader.readNext());
    assertNull(reader.readNext());
  }

  @Test
  public void testIterator() throws CsvValidationException, IOException {
    mockReader();
    CsvReader reader = new CsvReader(csvReader, null);
    List<DynamicObject> lines = new ArrayList<>();
    reader.iterator().forEachRemaining(lines::add);
    assertEquals(List.of(
        new DynamicCsvLine(List.of("a", "b", "c")),
        new DynamicCsvLine(List.of("1", "2", "3"))
    ), lines);
  }

  @Test
  public void testIterable() throws CsvValidationException, IOException {
    mockReader();
    CsvReader reader = new CsvReader(csvReader, null);
    List<DynamicObject> lines = new ArrayList<>();
    for (DynamicObject line : reader) {
      lines.add(line);
    }
    assertEquals(List.of(
        new DynamicCsvLine(List.of("a", "b", "c")),
        new DynamicCsvLine(List.of("1", "2", "3"))
    ), lines);
  }

  @Test
  public void testReadAll() throws CsvValidationException, IOException {
    mockReader();
    CsvReader reader = new CsvReader(csvReader, null);
    assertEquals(List.of(
        new DynamicCsvLine(List.of("a", "b", "c")),
        new DynamicCsvLine(List.of("1", "2", "3"))
    ), reader.readAll());
  }

  @Test
  public void testFindLines() throws CsvValidationException, IOException {
    mockReader();
    CsvReader reader = new CsvReader(csvReader, null);

    assertEquals(DynamicObject.of(
            List.of(
                new DynamicCsvLine(List.of("a", "b", "c"))
            )
        ),
        reader.findLines((l) -> DynamicObject.of(l.getAt(0).getValue().equals("a")))
    );
  }

  @Test
  public void testMapLines() throws CsvValidationException, IOException {
    mockReader();
    CsvReader reader = new CsvReader(csvReader, null);

    assertEquals(DynamicObject.of(
            List.of(
                new DynamicCsvLine(DefaultMarcelMethods.map(List.of("a_mapped", "b_mapped", "c_mapped"), DynamicObject::of)),
                new DynamicCsvLine(DefaultMarcelMethods.map(List.of("1_mapped", "2_mapped", "3_mapped"), DynamicObject::of))
            )
        ),
        reader.mapLines((l) -> DynamicObject.of(l.map(cell -> DynamicObject.of(cell + "_mapped"))))
    );
  }

  @Test
  public void testFindLine() throws CsvValidationException, IOException {
    mockReader();
    CsvReader reader = new CsvReader(csvReader, null);

    assertEquals(new DynamicCsvLine(List.of("1", "2", "3")),
        reader.findLine((l) -> DynamicObject.of(l.getAt(0).getValue().equals("1")))
    );
  }
  private void mockReader() throws CsvValidationException, IOException {
    when(csvReader.readNext()).thenReturn(
        new String[] { "a", "b", "c" },
        new String[] { "1", "2", "3" },
        null
    );
  }
}
