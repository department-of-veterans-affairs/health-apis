package gov.va.api.health.sentinel.crawler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.api.elements.Reference;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * This class provides a customized Jackson mapper that will intercept and record URLs of all
 * reference objects.
 *
 * <ul>
 *   <li>Order of references in not guaranteed.
 *   <li>This class is NOT thread safe.
 *   <li>Instances of this class or the mappers created by it may be used more once. References will
 *       be accumulated.
 * </ul>
 *
 * <p>Order is not guaranteed.
 *
 * <pre>
 * ReferenceInterceptor interceptor = new ReferenceInterceptor();
 * Thing t = interceptor.mapper().readValue(json,Thing.class);
 * interceptor.references().forEach(url -> coolLink(url));
 * </pre>
 */
@Slf4j
class ReferenceInterceptor {
  @Getter private final Set<String> references = new HashSet<>();

  /**
   * Return a new mapper that can be used to collect references. Call `references()` after this
   * method.
   */
  ObjectMapper mapper() {
    ObjectMapper mapper = JacksonConfig.createMapper();
    SimpleModule cheat = new SimpleModule();
    cheat.setDeserializerModifier(new ReferenceDeserializerModifier());
    mapper.registerModule(cheat);
    return mapper;
  }

  /**
   * This modifier will wrap the normal deserializer. Values returned will be inspected. References
   * with a valid URL specified will be recorded.
   */
  private class ReferenceDeserializer extends BeanDeserializer {

    ReferenceDeserializer(BeanDeserializer deserializer) {
      super(deserializer);
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt)
        throws IOException, JsonProcessingException {
      Object value = super.deserialize(p, ctxt);
      if (value instanceof Reference) {
        Reference reference = (Reference) value;
        if (StringUtils.isNotBlank(reference.reference())) {
          if (references.add(reference.reference())) {
            log.debug("Found reference to '{}' ({})", reference.display(), reference.reference());
          }
        }
      }
      return value;
    }
  }

  /**
   * This janky modifier is used to intercept the deserializer configuration for the Reference
   * class. It will install our custom deserializer instead.
   */
  private class ReferenceDeserializerModifier extends BeanDeserializerModifier {
    @Override
    public JsonDeserializer<?> modifyDeserializer(
        DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
      if (Reference.class.isAssignableFrom(beanDesc.getBeanClass())
          && deserializer instanceof BeanDeserializer) {
        return new ReferenceDeserializer((BeanDeserializer) deserializer);
      }
      return deserializer;
    }
  }
}
