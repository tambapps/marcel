package marcel.csv;

import marcel.lang.DynamicObject;
import marcel.lang.dynamic.MissingPropertyException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DynamicCsvLineTest {

  private final List<String> values = List.of("one", "2", "three");
  private final DynamicCsvLine headerlessLine = new DynamicCsvLine(values);
  private final List<String> headers = List.of("category 1", "two", "suree");
  private final DynamicCsvLine line = new DynamicCsvLine(headers, values);

  @Test
  void testGetAt() {
    for (int i = 0; i < values.size(); i++) {
      assertEquals(DynamicObject.of(values.get(i)), headerlessLine.getAt(i));
    }
    for (int i = 0; i < values.size(); i++) {
      assertEquals(DynamicObject.of(values.get(i)), line.getAt(i));
    }
  }

  @Test
  void testGetProperty() {
    for (String header : headers) {
      assertThrows(MissingPropertyException.class, () -> headerlessLine.getProperty(header));
    }
    for (int i = 0; i < headers.size(); i++) {
      assertEquals(DynamicObject.of(values.get(i)), line.getProperty(headers.get(i)));
    }
  }

  @Test
  void testAsMap() {
    assertEquals(Map.of(0, "one", 1, "2", 2, "three"), headerlessLine.asMap());
    assertEquals(Map.of("category 1", "one", "two", "2", "suree", "three"), line.asMap());
  }

  @Test
  void testAsList() {
    assertEquals(List.of("one", "2", "three"), headerlessLine.asList());
    assertEquals(List.of("one", "2", "three"), line.asList());
  }

  @Test
  void testEquals() {
    assertEquals(line, headerlessLine);
    assertNotEquals(line, new DynamicCsvLine(List.of("4", "2")));
    assertNotEquals(line, new DynamicCsvLine(headers, List.of("4", "2")));

  }
}
