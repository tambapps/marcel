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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicJson {

  // useful to handle json without having to instantiate a mapper
  private static DynamicJson _instance = new DynamicJson();

  public static DynamicJson getInstance() {
    if (_instance == null) {
      _instance = new DynamicJson();
    }
    return _instance;
  }

  protected final ObjectMapper mapper;

  public DynamicJson() {
    this(newObjectMapper());
  }

  public DynamicJson(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public DynamicObject read(String text) throws JsonProcessingException {
    return read(text, DynamicObject.class);
  }

  public DynamicObject read(byte[] bytes) throws IOException {
    return read(bytes, DynamicObject.class);
  }

  public DynamicObject read(InputStream inputStream) throws IOException {
    return read(inputStream, DynamicObject.class);
  }

  public DynamicObject read(File file) throws IOException {
    return read(file, DynamicObject.class);
  }

  public DynamicObject read(Reader reader) throws IOException {
    return read(reader, DynamicObject.class);
  }

  public DynamicObject readFile(String path) throws IOException {
    return read(new File(path), DynamicObject.class);
  }

  public <T> T read(String text, Class<T> clazz) throws JsonProcessingException {
    return mapper.readValue(text, clazz);
  }

  public <T> T read(byte[] bytes, Class<T> clazz) throws IOException {
    return mapper.readValue(bytes, clazz);
  }

  public <T> T read(InputStream inputStream, Class<T> clazz) throws IOException {
    return mapper.readValue(inputStream, clazz);
  }

  public <T> T read(File file, Class<T> clazz) throws IOException {
    return mapper.readValue(file, clazz);
  }

  public <T> T read(Reader reader, Class<T> clazz) throws IOException {
    return mapper.readValue(reader, clazz);
  }

  public <T> T readFile(String path, Class<T> clazz) throws IOException {
    return read(new File(path), clazz);
  }

  public String writeAsString(Object o) throws JsonProcessingException {
    return mapper.writeValueAsString(o);
  }

  public String writeAsPrettyString(Object o) throws JsonProcessingException {
    return mapper.writerWithDefaultPrettyPrinter()
        .writeValueAsString(o);
  }

  public byte[] writeAsBytes(Object o) throws JsonProcessingException {
    return mapper.writeValueAsBytes(o);
  }

  public void write(Object o, File file) throws IOException {
    mapper.writeValue(file, o);
  }

  public void writeInFile(Object o, String path) throws IOException {
    write(o, new File(path));
  }

  public void write(Object o, OutputStream outputStream) throws IOException {
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

  public static ObjectMapper newObjectMapper() {
    return newObjectMapper(new ObjectMapper());
  }

  public static ObjectMapper newObjectMapper(ObjectMapper mapper) {
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    SimpleModule module = new SimpleModule();
    module.addSerializer(DynamicObject.class, new DynamicObjectSerializer());
    module.addDeserializer(DynamicObject.class, new DynamicObjectDeserializer());
    mapper.registerModule(module);
    return mapper;
  }
}
