package marcel.yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import marcel.lang.DynamicObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class YamlSlurperTest {

  private final YamlSlurper slurper = new YamlSlurper();

  @Test
  void testSlurpInt() throws JsonProcessingException {
    assertEquals(1, slurper.slurp("1").asInt());
  }

  @Test
  void testSlurpString() throws JsonProcessingException {
    assertEquals("Hello World", slurper.slurp("\"Hello World\"").asString());
  }

  @Test
  void testSlurpObject() throws JsonProcessingException {
    DynamicObject object = slurper.slurp("a: b\nc: 23");
    assertEquals("b", object.getAt("a").asString());
    assertEquals(23, object.getAt("c").asInt());

    Map<String, Object> map = new HashMap<>();
    map.put("a", "b");
    map.put("c", 23);
    assertEquals(map, object.asMap());
  }

  @Test
  void testSerializeInt() throws JsonProcessingException {
    assertEquals("1", slurper.serializeToString(1).trim());
    assertEquals("1", slurper.serializeToString(DynamicObject.of(1)).trim());
  }

  @Test
  void testSerializeString() throws JsonProcessingException {
    assertEquals("\"Hello World\"", slurper.serializeToString("Hello World").trim());
    assertEquals("\"Hello World\"", slurper.serializeToString(DynamicObject.of("Hello World")).trim());
  }

  @Test
  void testSerializeMap() throws JsonProcessingException {
    Map<String, Object> map = new HashMap<>();
    map.put("a", "b");
    map.put("c", 23);

    String expected = "a: \"b\"\n" +
        "c: 23";
    assertEquals(expected, slurper.serializeToString(map).trim());
    assertEquals(expected, slurper.serializeToString(DynamicObject.of(map)).trim());
  }
}
