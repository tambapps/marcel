package marcel.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import marcel.lang.DynamicObject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSlurperTest {

  private final JsonSlurper slurper = new JsonSlurper();

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
    DynamicObject object = slurper.slurp("{\"a\": \"b\", \"c\": 23}");
    assertEquals("b", object.getAt("a").asString());
    assertEquals(23, object.getAt("c").asInt());

    Map<String, Object> map = new HashMap<>();
    map.put("a", "b");
    map.put("c", 23);
    assertEquals(map, object.asMap());
  }

  @Test
  void testSerializeInt() throws JsonProcessingException {
    assertEquals("1", slurper.serializeToString(1));
    assertEquals("1", slurper.serializeToString(DynamicObject.of(1)));
  }

  @Test
  void testSerializeString() throws JsonProcessingException {
    assertEquals("\"Hello World\"", slurper.serializeToString("Hello World"));
    assertEquals("\"Hello World\"", slurper.serializeToString(DynamicObject.of("Hello World")));
  }

  @Test
  void testSerializeMap() throws JsonProcessingException {
    Map<String, Object> map = new HashMap<>();
    map.put("a", "b");
    map.put("c", 23);

    assertEquals("{\"a\":\"b\",\"c\":23}", slurper.serializeToString(map));
    assertEquals("{\"a\":\"b\",\"c\":23}", slurper.serializeToString(DynamicObject.of(map)));
  }
}
