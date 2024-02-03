package marcel.yaml;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import marcel.json.JsonSlurper;

public class YamlSlurper extends JsonSlurper {

  public YamlSlurper() {
    this(newObjectMapper(new ObjectMapper(newYamlFactory())));
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  private static JsonFactory newYamlFactory() {
    return new YAMLFactory()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
  }

  public YamlSlurper(ObjectMapper mapper) {
    super(mapper);
  }

}
