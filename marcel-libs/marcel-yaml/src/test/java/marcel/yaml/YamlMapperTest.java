package marcel.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import marcel.lang.DynamicObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YamlMapperTest {

  private final YamlMapper slurper = new YamlMapper();

  @Test
  void testSlurpInt() throws JsonProcessingException {
    assertEquals(1, slurper.read("1").asInt());
  }

  @Test
  void testSlurpString() throws JsonProcessingException {
    assertEquals("Hello World", slurper.read("\"Hello World\"").asString());
  }

  @Test
  void testSlurpObject() throws JsonProcessingException {
    DynamicObject object = slurper.read("a: b\nc: 23");
    assertEquals("b", object.getAt("a").asString());
    assertEquals(23, object.getAt("c").asInt());

    Map<String, Object> map = new HashMap<>();
    map.put("a", "b");
    map.put("c", 23);
    assertEquals(map, object.asMap());
  }

  @Test
  void testSerializeInt() throws JsonProcessingException {
    assertEquals("1", slurper.writeAsString(1).trim());
    assertEquals("1", slurper.writeAsString(DynamicObject.of(1)).trim());
  }

  @Test
  void testSerializeString() throws JsonProcessingException {
    assertEquals("\"Hello World\"", slurper.writeAsString("Hello World").trim());
    assertEquals("\"Hello World\"", slurper.writeAsString(DynamicObject.of("Hello World")).trim());
  }

  @Test
  void testSerializeMap() throws JsonProcessingException {
    Map<String, Object> map = new HashMap<>();
    map.put("a", "b");
    map.put("c", 23);

    String expected = "a: \"b\"\n" +
        "c: 23";
    assertEquals(expected, slurper.writeAsString(map).trim());
    assertEquals(expected, slurper.writeAsString(DynamicObject.of(map)).trim());
  }
}
