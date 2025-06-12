package marcel.csv;

import marcel.lang.DynamicObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamicCsvIntegrationTest {

  @Test
  void testWithHeaders() throws Exception {
    String csv = """
        companyName,url,score
        Foo Inc,http://example.foo/,24
        Bar Corporation,http://example.bar/,0
        """;
    try (CsvReader reader = new DynamicCsv().reader(csv, null, null, null, true, null)) {
      DynamicObject line1 = reader.readNext();
      assertEquals(DynamicObject.of("Foo Inc"), line1.getAt(0));
      assertEquals(DynamicObject.of("Foo Inc"), line1.getAt("companyName"));
      assertEquals(DynamicObject.of("http://example.foo/"), line1.getAt(1));
      assertEquals(24, line1.getAt(2).asInt());

      DynamicObject line2 = reader.readNext();
      assertEquals(DynamicObject.of("Bar Corporation"), line2.getAt(0));
      assertEquals(DynamicObject.of("http://example.bar/"), line2.getAt("url"));
      assertEquals(DynamicObject.of("http://example.bar/"), line2.getAt(1));
      assertEquals(0, line2.getAt(2).asInt());
    }
  }
}
