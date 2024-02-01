package marcel.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import marcel.lang.DynamicObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonSlurper {
  private final ObjectMapper mapper;

  public JsonSlurper() {
    this(newObjectMapper());
  }

  public JsonSlurper(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public DynamicObject slurp(String text) throws JsonProcessingException {
    JsonNode node = mapper.readTree(text);
    return DynamicObject.of(toObject(node));
  }

  public String serializeToString(Object o) throws JsonProcessingException {
    return mapper.writeValueAsString(o);
  }

  public byte[] serializeToBytes(Object o) throws JsonProcessingException {
    return mapper.writeValueAsBytes(o);
  }

  public void serialize(Object o, File file) throws IOException {
    mapper.writeValue(file, o);
  }

  public void serializeInFile(Object o, String path) throws IOException {
    serialize(o, new File(path));
  }

  public void serializeIn(Object o, OutputStream outputStream) throws IOException {
    mapper.writeValue(outputStream, o);
  }

  @SneakyThrows
  public static Object toObject(JsonNode node) {
    if (node.isDouble()) return node.asDouble();
    else if (node.isFloat()) return node.floatValue();
    else if (node.isLong()) return node.asLong();
    else if (node.isInt()) return node.asInt();
    else if (node.isBigInteger()) return node.bigIntegerValue();
    else if (node.isTextual()) return node.asText();
    else if (node.isBoolean()) return node.asBoolean();
    else if (node.isBinary()) return node.binaryValue();
    else if (node.isNull()) return null;
    else if (node.isArray()) {
      List<Object> list = new ArrayList<>(node.size());
      for (int i = 0; i < node.size(); i++) {
        list.add(toObject(node.get(i)));
      }
      return list;
    } else if (node.isObject()) {
      Map<String, Object> map = new HashMap<>();
      node.fields().forEachRemaining(e -> map.put(e.getKey(), toObject(e.getValue())));
      return map;
    } else {
      throw new RuntimeException("Internal error, doesn't handle node " + node);
    }
  }

  private  static ObjectMapper newObjectMapper() {
    final ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    SimpleModule module = new SimpleModule();
    module.addSerializer(DynamicObject.class, new DynamicObjectSerializer());
    module.addDeserializer(DynamicObject.class, new DynamicObjectDeserializer());
    mapper.registerModule(module);
    return mapper;
  }
}
