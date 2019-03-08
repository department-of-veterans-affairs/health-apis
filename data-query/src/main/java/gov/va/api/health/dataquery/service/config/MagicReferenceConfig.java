package gov.va.api.health.dataquery.service.config;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import gov.va.api.health.dataquery.api.DataAbsentReason;
import gov.va.api.health.dataquery.api.DataAbsentReason.Reason;
import gov.va.api.health.dataquery.api.elements.Extension;
import gov.va.api.health.dataquery.api.elements.Reference;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * This class provides the Jackson magic necessary to globally apply special logic for References.
 * It will
 *
 * <ul>
 *   <li>Automatically fully qualify reference URLs with a configurable base url and path
 *   <li>Automatically filter out references for resources that are optional, such as Appointment
 * </ul>
 *
 * The goal of this class is to minimize impact of reference logic through out the application code
 * base. Instead, the above rules are applied universally during serialization. Unfortunately, to
 * accomplish all of this, we have to get deep in the bowel of Jackson.
 *
 * <ul>
 *   <li>To omit fields that are references to optional resources, we will create a property filter
 *       and apply to all objects using a mix-in. The filter will inspect the value of the field to
 *       determine if the value should be omitted.
 *   <li>To omit references in lists, a bean serialization customizer will be attached.
 *   <li>To fully qualify references, a bean property customizer will be attached.
 * </ul>
 */
@Slf4j
@Component
public class MagicReferenceConfig {
  /**
   * The published URL for data-query, which is likely not the hostname of the machine running this
   * application.
   */
  private final String baseUrl;
  /** These base path for resources, e.g. api */
  private final String basePath;
  /** Property defining the references to serialize. */
  private final ReferenceSerializerProperties config;

  /** Auto-wired constructor. */
  @Autowired
  public MagicReferenceConfig(
      @Value("${argonaut.url}") String baseUrl,
      @Value("${argonaut.base-path}") String basePath,
      ReferenceSerializerProperties config) {
    this.baseUrl = baseUrl;
    this.basePath = basePath;
    this.config = config;
    log.info("{}", config);
  }

  /**
   * Configure and return the given mapper to support magic references as described in the class
   * documentation.
   */
  public ObjectMapper configure(ObjectMapper mapper) {
    mapper.registerModule(new MagicReferenceModule());
    mapper.addMixIn(Object.class, ApplyOptionalReferenceFilter.class);
    mapper.setFilterProvider(
        new SimpleFilterProvider().addFilter("magic-references", new OptionalReferencesFilter()));
    return mapper;
  }

  private boolean hasExtensionField(Object object, String name) {
    try {
      Field field = object.getClass().getDeclaredField(name);
      return field.getType() == Extension.class;
    } catch (NoSuchFieldException e) {
      return false;
    }
  }

  /**
   * This mix-in is applied to all objects and used to trigger optional reference filter on all
   * fields.
   */
  @JsonFilter("magic-references")
  private static class ApplyOptionalReferenceFilter {}

  /**
   * This filter inspect values of fields. If a field value is a reference that has been disabled,
   * the field will be omitted.
   */
  private class OptionalReferencesFilter extends SimpleBeanPropertyFilter {
    /**
     * This is a little gross and only filters when the writer is a bean property writer. We need
     * that type of writer so we can peek at the value we are about to serialize.
     */
    @Override
    public void serializeAsField(
        Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer)
        throws Exception {
      boolean include = true;
      if (writer.getType().getRawClass() == Reference.class
          && writer instanceof BeanPropertyWriter) {
        Reference value = (Reference) ((BeanPropertyWriter) writer).get(pojo);
        include = config.isEnabled(value);
      }

      if (include) {
        writer.serializeAsField(pojo, jgen, provider);
      } else {
        /*
         * Since the field isn't included, we need to emit a Data Absent Reason if the field is
         * required. Required fields can be detected by finding an underscore prefixed version of
         * type Extension.
         */
        String extensionField = "_" + writer.getName();
        if (hasExtensionField(pojo, extensionField)) {
          jgen.writeObjectField(extensionField, DataAbsentReason.of(Reason.unsupported));
        } else if (!jgen.canOmitFields()) {
          writer.serializeAsOmittedField(pojo, jgen, provider);
        }
      }
    }
  }

  /**
   * This serializer is fired for references _in_ a list. The {@link OptionalReferencesFilter} is
   * responsible for making sure the field references are omitted.
   */
  private class OptionalReferenceSerializer extends JsonSerializer<Reference> {
    /**
     * This is the default serializer used for references, we will delegate the hard parts to it.
     */
    private JsonSerializer<Reference> delegate;

    OptionalReferenceSerializer(JsonSerializer<Reference> delegate) {
      this.delegate = delegate;
    }

    /**
     * If the resource reference is well formed, extract the name, and check if it is an enabled
     * reference. Otherwise if it is malformed, always use default serialization.
     */
    @Override
    public void serialize(Reference value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException {
      if (value == null) {
        return;
      }
      if (config.isEnabled(value)) {
        delegate.serialize(value, jgen, provider);
      }
    }
  }

  /**
   * This module is the vehicle used to add a bean serialization modifiers for both fully qualified
   * URLs and magically omitted reference entries in lists.
   */
  private class MagicReferenceModule extends SimpleModule {
    @Override
    public void setupModule(SetupContext context) {
      super.setupModule(context);
      context.addBeanSerializerModifier(
          new BeanSerializerModifier() {
            @Override
            public List<BeanPropertyWriter> changeProperties(
                SerializationConfig serialConfig,
                BeanDescription beanDesc,
                List<BeanPropertyWriter> beanProperties) {
              if (beanDesc.getBeanClass() == Reference.class) {
                for (int i = 0; i < beanProperties.size(); i++) {
                  BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);
                  if ("reference".equals(beanPropertyWriter.getName())) {
                    beanProperties.set(i, new QualifiedReferenceWriter(beanPropertyWriter));
                  }
                }
              }

              return super.changeProperties(serialConfig, beanDesc, beanProperties);
            }

            @Override
            @SuppressWarnings("unchecked")
            public JsonSerializer<?> modifySerializer(
                SerializationConfig serialConfig,
                BeanDescription beanDesc,
                JsonSerializer<?> serializer) {
              if (Reference.class.isAssignableFrom(beanDesc.getBeanClass())) {
                //noinspection unchecked
                return new OptionalReferenceSerializer((JsonSerializer<Reference>) serializer);
              }
              return super.modifySerializer(serialConfig, beanDesc, serializer);
            }
          });
    }
  }

  /** Provides fully qualified URLs for references. */
  private class QualifiedReferenceWriter extends BeanPropertyWriter {
    private QualifiedReferenceWriter(BeanPropertyWriter base) {
      super(base);
    }

    private String qualify(String reference) {
      if (StringUtils.isBlank(reference)) {
        return null;
      }
      if (reference.startsWith("http")) {
        return reference;
      }
      if (reference.startsWith("/")) {
        return baseUrl + "/" + basePath + reference;
      }
      return baseUrl + "/" + basePath + "/" + reference;
    }

    @Override
    public void serializeAsField(
        Object shouldBeReference, JsonGenerator gen, SerializerProvider prov) throws Exception {
      if (!(shouldBeReference instanceof Reference)) {
        throw new IllegalArgumentException(
            "Qualified reference writer cannot serialize: " + shouldBeReference);
      }
      Reference reference = (Reference) shouldBeReference;
      String qualifiedReference = qualify(reference.reference());
      if (qualifiedReference != null) {
        gen.writeStringField(getName(), qualifiedReference);
      }
    }
  }
}
