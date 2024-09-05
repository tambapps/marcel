package marcel.yaml;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import marcel.json.JsonMapper;

public class YamlMapper extends JsonMapper {

  // useful to handle json without having to instantiate a mapper
  private static YamlMapper _instance = new YamlMapper();

  public static YamlMapper getInstance() {
    if (_instance == null) {
      _instance = new YamlMapper();
    }
    return _instance;
  }

  public YamlMapper() {
    this(newObjectMapper(new ObjectMapper(newYamlFactory())));
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  private static JsonFactory newYamlFactory() {
    return new YAMLFactory()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
  }

  public YamlMapper(ObjectMapper mapper) {
    super(mapper);
  }

}
