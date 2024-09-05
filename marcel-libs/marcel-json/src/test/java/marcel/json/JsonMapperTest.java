package marcel.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import marcel.lang.DynamicObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonMapperTest {

  private final JsonMapper slurper = new JsonMapper();

  @Test
  void testReadInt() throws JsonProcessingException {
    assertEquals(1, slurper.read("1").asInt());
  }

  @Test
  void testReadString() throws JsonProcessingException {
    assertEquals("Hello World", slurper.read("\"Hello World\"").asString());
  }

  @Test
  void testReadObject() throws JsonProcessingException {
    DynamicObject object = slurper.read("{\"a\": \"b\", \"c\": 23}");
    assertEquals("b", object.getAt("a").asString());
    assertEquals(23, object.getAt("c").asInt());

    Map<String, Object> map = new HashMap<>();
    map.put("a", "b");
    map.put("c", 23);
    assertEquals(map, object.asMap());
  }

  @Test
  void testSerializeInt() throws JsonProcessingException {
    assertEquals("1", slurper.writeAsString(1));
    assertEquals("1", slurper.writeAsString(DynamicObject.of(1)));
  }

  @Test
  void testSerializeString() throws JsonProcessingException {
    assertEquals("\"Hello World\"", slurper.writeAsString("Hello World"));
    assertEquals("\"Hello World\"", slurper.writeAsString(DynamicObject.of("Hello World")));
  }

  @Test
  void testSerializeMap() throws JsonProcessingException {
    Map<String, Object> map = new HashMap<>();
    map.put("a", "b");
    map.put("c", 23);

    assertEquals("{\"a\":\"b\",\"c\":23}", slurper.writeAsString(map));
    assertEquals("{\"a\":\"b\",\"c\":23}", slurper.writeAsString(DynamicObject.of(map)));
  }
}
