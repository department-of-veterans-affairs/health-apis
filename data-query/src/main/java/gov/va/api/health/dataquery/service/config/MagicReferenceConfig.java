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
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import lombok.SneakyThrows;
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

  /** These base path for DSTU2 resources, e.g. api/dstu2. */
  private final String dstu2BasePath;

  /** These base path for STU3 resources, e.g. api/stu3. */
  private final String stu3BasePath;

  /** Property defining the references to serialize. */
  private final ReferenceSerializerProperties config;

  /** Auto-wired constructor. */
  @Autowired
  public MagicReferenceConfig(
      @Value("${argonaut.url}") String baseUrl,
      @Value("${argonaut.dstu2-base-path}") String dstu2BasePath,
      @Value("${argonaut.stu3-base-path}") String stu3BasePath,
      ReferenceSerializerProperties config) {
    this.baseUrl = baseUrl;
    this.dstu2BasePath = dstu2BasePath;
    this.stu3BasePath = stu3BasePath;
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

  /**
   * This mix-in is applied to all objects and used to trigger optional reference filter on all
   * fields.
   */
  @JsonFilter("magic-references")
  private static class ApplyOptionalReferenceFilter {}

  /**
   * This serializer is fired for references _in_ a list. The {@link OptionalReferencesFilter} is
   * responsible for making sure the field references are omitted.
   */
  private final class Dstu2OptionalReferenceSerializer
      extends JsonSerializer<gov.va.api.health.dstu2.api.elements.Reference> {
    /**
     * This is the default serializer used for references, we will delegate the hard parts to it.
     */
    private JsonSerializer<gov.va.api.health.dstu2.api.elements.Reference> delegate;

    Dstu2OptionalReferenceSerializer(
        JsonSerializer<gov.va.api.health.dstu2.api.elements.Reference> delegate) {
      this.delegate = delegate;
    }

    /**
     * If the resource reference is well formed, extract the name, and check if it is an enabled
     * reference. Otherwise if it is malformed, always use default serialization.
     */
    @Override
    public void serialize(
        gov.va.api.health.dstu2.api.elements.Reference value,
        JsonGenerator jgen,
        SerializerProvider provider)
        throws IOException {
      if (value == null) {
        return;
      }
      if (config.isEnabled(value)) {
        delegate.serialize(value, jgen, provider);
      }
    }
  }

  /** Provides fully qualified URLs for references. */
  private final class Dstu2QualifiedReferenceWriter extends BeanPropertyWriter {
    private Dstu2QualifiedReferenceWriter(BeanPropertyWriter base) {
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
        return baseUrl + "/" + dstu2BasePath + reference;
      }
      return baseUrl + "/" + dstu2BasePath + "/" + reference;
    }

    @Override
    @SneakyThrows
    public void serializeAsField(
        Object shouldBeReference, JsonGenerator gen, SerializerProvider prov) {
      if (!(shouldBeReference instanceof gov.va.api.health.dstu2.api.elements.Reference)) {
        throw new IllegalArgumentException(
            "DSTU2 Qualified reference writer cannot serialize: " + shouldBeReference);
      }
      gov.va.api.health.dstu2.api.elements.Reference reference =
          (gov.va.api.health.dstu2.api.elements.Reference) shouldBeReference;
      String qualifiedReference = qualify(reference.reference());
      if (qualifiedReference != null) {
        gen.writeStringField(getName(), qualifiedReference);
      }
    }
  }

  /**
   * This module is the vehicle used to add a bean serialization modifiers for both fully qualified
   * URLs and magically omitted reference entries in lists.
   */
  private final class MagicReferenceModule extends SimpleModule {
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

              if (beanDesc.getBeanClass() == gov.va.api.health.dstu2.api.elements.Reference.class) {
                for (int i = 0; i < beanProperties.size(); i++) {
                  BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);
                  if ("reference".equals(beanPropertyWriter.getName())) {
                    beanProperties.set(i, new Dstu2QualifiedReferenceWriter(beanPropertyWriter));
                  }
                }
              }

              if (beanDesc.getBeanClass() == gov.va.api.health.stu3.api.elements.Reference.class) {
                for (int i = 0; i < beanProperties.size(); i++) {
                  BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);
                  if ("reference".equals(beanPropertyWriter.getName())) {
                    beanProperties.set(i, new Stu3QualifiedReferenceWriter(beanPropertyWriter));
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

              if (gov.va.api.health.dstu2.api.elements.Reference.class.isAssignableFrom(
                  beanDesc.getBeanClass())) {
                return new Dstu2OptionalReferenceSerializer(
                    (JsonSerializer<gov.va.api.health.dstu2.api.elements.Reference>) serializer);
              }
              if (gov.va.api.health.stu3.api.elements.Reference.class.isAssignableFrom(
                  beanDesc.getBeanClass())) {
                return new Stu3OptionalReferenceSerializer(
                    (JsonSerializer<gov.va.api.health.stu3.api.elements.Reference>) serializer);
              }

              return super.modifySerializer(serialConfig, beanDesc, serializer);
            }
          });
    }
  }

  /**
   * This filter inspect values of fields. If a field value is a reference that has been disabled,
   * the field will be omitted.
   */
  private final class OptionalReferencesFilter extends SimpleBeanPropertyFilter {
    private boolean hasDstu2ExtensionField(Object object, String name) {
      try {
        Field field = object.getClass().getDeclaredField(name);
        return field.getType() == gov.va.api.health.dstu2.api.elements.Extension.class;
      } catch (NoSuchFieldException e) {
        return false;
      }
    }

    private boolean hasStu3ExtensionField(Object object, String name) {
      try {
        Field field = object.getClass().getDeclaredField(name);
        return field.getType() == gov.va.api.health.stu3.api.elements.Extension.class;
      } catch (NoSuchFieldException e) {
        return false;
      }
    }

    /**
     * This is a little gross and only filters when the writer is a bean property writer. We need
     * that type of writer so we can peek at the value we are about to serialize.
     */
    @Override
    @SneakyThrows
    public void serializeAsField(
        Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) {

      boolean include = true;

      if (writer.getType().getRawClass() == gov.va.api.health.dstu2.api.elements.Reference.class
          && writer instanceof BeanPropertyWriter) {
        gov.va.api.health.dstu2.api.elements.Reference dstu2Reference =
            (gov.va.api.health.dstu2.api.elements.Reference)
                ((BeanPropertyWriter) writer).get(pojo);
        include = config.isEnabled(dstu2Reference);
      }
      if (writer.getType().getRawClass() == gov.va.api.health.stu3.api.elements.Reference.class
          && writer instanceof BeanPropertyWriter) {
        gov.va.api.health.stu3.api.elements.Reference stu3Reference =
            (gov.va.api.health.stu3.api.elements.Reference) ((BeanPropertyWriter) writer).get(pojo);
        include = config.isEnabled(stu3Reference);
      }

      if (include) {
        writer.serializeAsField(pojo, jgen, provider);
        return;
      }

      /*
       * Since the field isn't included, we need to emit a Data Absent Reason if the field is
       * required. Required fields can be detected by finding an underscore prefixed version of
       * type Extension.
       */
      String extensionField = "_" + writer.getName();
      if (hasDstu2ExtensionField(pojo, extensionField)) {
        jgen.writeObjectField(
            extensionField,
            gov.va.api.health.dstu2.api.DataAbsentReason.of(
                gov.va.api.health.dstu2.api.DataAbsentReason.Reason.unsupported));
      } else if (hasStu3ExtensionField(pojo, extensionField)) {
        jgen.writeObjectField(
            extensionField,
            gov.va.api.health.stu3.api.DataAbsentReason.of(
                gov.va.api.health.stu3.api.DataAbsentReason.Reason.unsupported));
      } else if (!jgen.canOmitFields()) {
        writer.serializeAsOmittedField(pojo, jgen, provider);
      }
    }
  }

  /**
   * This serializer is fired for references _in_ a list. The {@link OptionalReferencesFilter} is
   * responsible for making sure the field references are omitted.
   */
  private final class Stu3OptionalReferenceSerializer
      extends JsonSerializer<gov.va.api.health.stu3.api.elements.Reference> {
    /**
     * This is the default serializer used for references, we will delegate the hard parts to it.
     */
    private JsonSerializer<gov.va.api.health.stu3.api.elements.Reference> delegate;

    Stu3OptionalReferenceSerializer(
        JsonSerializer<gov.va.api.health.stu3.api.elements.Reference> delegate) {
      this.delegate = delegate;
    }

    /**
     * If the resource reference is well formed, extract the name, and check if it is an enabled
     * reference. Otherwise if it is malformed, always use default serialization.
     */
    @Override
    public void serialize(
        gov.va.api.health.stu3.api.elements.Reference value,
        JsonGenerator jgen,
        SerializerProvider provider)
        throws IOException {
      if (value == null) {
        return;
      }
      if (config.isEnabled(value)) {
        delegate.serialize(value, jgen, provider);
      }
    }
  }

  /** Provides fully qualified URLs for references. */
  private final class Stu3QualifiedReferenceWriter extends BeanPropertyWriter {
    private Stu3QualifiedReferenceWriter(BeanPropertyWriter base) {
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
        return baseUrl + "/" + stu3BasePath + reference;
      }
      return baseUrl + "/" + stu3BasePath + "/" + reference;
    }

    @Override
    @SneakyThrows
    public void serializeAsField(
        Object shouldBeReference, JsonGenerator gen, SerializerProvider prov) {
      if (!(shouldBeReference instanceof gov.va.api.health.stu3.api.elements.Reference)) {
        throw new IllegalArgumentException(
            "STU3 Qualified reference writer cannot serialize: " + shouldBeReference);
      }
      gov.va.api.health.stu3.api.elements.Reference reference =
          (gov.va.api.health.stu3.api.elements.Reference) shouldBeReference;
      String qualifiedReference = qualify(reference.reference());
      if (qualifiedReference != null) {
        gen.writeStringField(getName(), qualifiedReference);
      }
    }
  }
}
