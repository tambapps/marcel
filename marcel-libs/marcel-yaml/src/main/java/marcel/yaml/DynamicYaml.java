package marcel.yaml;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import marcel.json.DynamicJson;

public class DynamicYaml extends DynamicJson {

  // useful to handle yaml without having to instantiate a mapper
  private static DynamicYaml _instance = new DynamicYaml();

  public static DynamicYaml getInstance() {
    if (_instance == null) {
      _instance = new DynamicYaml();
    }
    return _instance;
  }

  public DynamicYaml() {
    this(newObjectMapper(new ObjectMapper(newYamlFactory())));
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  private static JsonFactory newYamlFactory() {
    return new YAMLFactory()
        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);
  }

  public DynamicYaml(ObjectMapper mapper) {
    super(mapper);
  }

}
