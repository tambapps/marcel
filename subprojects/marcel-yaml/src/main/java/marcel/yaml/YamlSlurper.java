package marcel.yaml;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import marcel.json.JsonSlurper;

public class YamlSlurper extends JsonSlurper {

  public YamlSlurper() {
    this(newObjectMapper(new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))));
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
  }

  public YamlSlurper(ObjectMapper mapper) {
    super(mapper);
  }

}
