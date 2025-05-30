package marcel.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import marcel.lang.DynamicObject;

import java.io.IOException;

public class DynamicObjectDeserializer extends JsonDeserializer<DynamicObject> {

  @Override
  public DynamicObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    JsonNode jsonNode = p.getCodec().readTree(p);
    return DynamicObject.of(DynamicJson.toObject(jsonNode));
  }
}
