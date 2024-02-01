package marcel.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import marcel.lang.DynamicObject;

import java.io.IOException;
class DynamicObjectSerializer extends JsonSerializer<DynamicObject> {

  @Override
  public void serialize(DynamicObject dynamicObject, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    if (dynamicObject == null) {
      serializers.getDefaultNullValueSerializer().serialize(null, gen, serializers);
    } else {
      Object value = dynamicObject.getValue();
      serializers.defaultSerializeValue(value, gen);
    }
  }
}
