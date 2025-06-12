package marcel.csv;

import marcel.lang.DynamicObject;
import marcel.lang.IntRanges;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamicCsvIntegrationTest {

  @Test
  void testReadWithHeaders() throws Exception {
    String csv = """
        companyName,url,score
        Foo Inc,http://example.foo/,24
        Bar Corporation,http://example.bar/,0
        """;
    try (CsvReader reader = DynamicCsv.reader(csv, null, null, null, true, null)) {
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
  @Test
  void testWrite() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (CsvWriter writer = DynamicCsv.writer(new OutputStreamWriter(baos), null, null, null, false)) {
      writer.write(new String[] { "companyName", "url", "score" });
      writer.write(new DynamicCsvLine(List.of("company A", "http://company.a/", 5)));
      writer.write(new Object[]{"company B", "http://company.b/", 25});
      writer.write(List.of("company, C", "http://company.c/", IntRanges.of(1, 10)));
    }
    assertEquals("""
        companyName,url,score
        company A,http://company.a/,5
        company B,http://company.b/,25
        "company, C",http://company.c/,1..10
        """, baos.toString());
  }

  @Test
  void testWriteApplyQuotesToAll() throws Exception {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (CsvWriter writer = DynamicCsv.writer(new OutputStreamWriter(baos), null, null, null, true)) {
      writer.write(new String[] { "companyName", "url", "score" });
      writer.write(new DynamicCsvLine(List.of("company A", "http://company.a/", 5)));
      writer.write(new Object[]{"company B", "http://company.b/", 25});
      writer.write(List.of("company, C", "http://company.c/", IntRanges.of(1, 10)));
    }
    assertEquals("""
        "companyName","url","score"
        "company A","http://company.a/","5"
        "company B","http://company.b/","25"
        "company, C","http://company.c/","1..10"
        """, baos.toString());
  }
}
