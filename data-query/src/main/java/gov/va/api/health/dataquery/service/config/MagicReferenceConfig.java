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
import gov.va.api.health.fhir.api.IsReference;
import java.lang.reflect.Field;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
 *   <li>Automatically filter out references for resources that are optional, such as Location
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

  /** These base path for R4 resources, e.g. r4. */
  private final String r4BasePath;

  /** Property defining the references to serialize. */
  private final ReferenceSerializerProperties config;

  /** Auto-wired constructor. */
  @Autowired
  public MagicReferenceConfig(
      @Value("${data-query.public-url}") String baseUrl,
      @Value("${data-query.public-dstu2-base-path}") String dstu2BasePath,
      @Value("${data-query.public-stu3-base-path}") String stu3BasePath,
      @Value("${data-query.public-r4-base-path}") String r4BasePath,
      ReferenceSerializerProperties config) {
    this.baseUrl = baseUrl;
    this.dstu2BasePath = dstu2BasePath;
    this.stu3BasePath = stu3BasePath;
    this.r4BasePath = r4BasePath;
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

  /** Provides fully qualified URLs for references. */
  private final class QualifiedReferenceWriter extends BeanPropertyWriter {

    private String basePath;

    private QualifiedReferenceWriter(BeanPropertyWriter base, String basePath) {
      super(base);
      this.basePath = basePath;
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
    @SneakyThrows
    public void serializeAsField(
        Object shouldBeReference, JsonGenerator gen, SerializerProvider prov) {
      if (!(shouldBeReference instanceof IsReference)) {
        throw new IllegalArgumentException(
            "Qualified reference writer cannot serialize: " + shouldBeReference);
      }
      IsReference reference = (IsReference) shouldBeReference;
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

            private void applyReferenceWriter(
                List<BeanPropertyWriter> beanProperties, String basePath) {
              for (int i = 0; i < beanProperties.size(); i++) {
                BeanPropertyWriter beanPropertyWriter = beanProperties.get(i);
                if ("reference".equals(beanPropertyWriter.getName())) {
                  beanProperties.set(i, new QualifiedReferenceWriter(beanPropertyWriter, basePath));
                }
              }
            }

            @Override
            public List<BeanPropertyWriter> changeProperties(
                SerializationConfig serialConfig,
                BeanDescription beanDesc,
                List<BeanPropertyWriter> beanProperties) {
              if (beanDesc.getBeanClass() == gov.va.api.health.dstu2.api.elements.Reference.class) {
                applyReferenceWriter(beanProperties, dstu2BasePath);
              }
              if (beanDesc.getBeanClass() == gov.va.api.health.stu3.api.elements.Reference.class) {
                applyReferenceWriter(beanProperties, stu3BasePath);
              }
              if (beanDesc.getBeanClass() == gov.va.api.health.r4.api.elements.Reference.class) {
                applyReferenceWriter(beanProperties, r4BasePath);
              }
              return super.changeProperties(serialConfig, beanDesc, beanProperties);
            }

            @Override
            @SuppressWarnings("unchecked")
            public JsonSerializer<?> modifySerializer(
                SerializationConfig serialConfig,
                BeanDescription beanDesc,
                JsonSerializer<?> serializer) {
              if (IsReference.class.isAssignableFrom(beanDesc.getBeanClass())) {
                return new OptionalReferenceSerializer<IsReference>(
                    (JsonSerializer<IsReference>) serializer);
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

    /**
     * If the type has the declared field with one of the known extension types, we will return an
     * unsupported data absent reason extension instance specific to the extension type, e.g. an
     * dstu2 or r4 extension. If the field is missing or not an extension, then we will emit
     * nothing.
     */
    private Object dataAbsentReasonForExtensionField(Object object, String name) {
      try {
        Field field = object.getClass().getDeclaredField(name);
        if (field.getType() == gov.va.api.health.dstu2.api.elements.Extension.class) {
          return gov.va.api.health.dstu2.api.DataAbsentReason.of(
              gov.va.api.health.dstu2.api.DataAbsentReason.Reason.unsupported);
        }
        if (field.getType() == gov.va.api.health.stu3.api.elements.Extension.class) {
          return gov.va.api.health.stu3.api.DataAbsentReason.of(
              gov.va.api.health.stu3.api.DataAbsentReason.Reason.unsupported);
        }
        if (field.getType() == gov.va.api.health.r4.api.elements.Extension.class) {
          return gov.va.api.health.r4.api.DataAbsentReason.of(
              gov.va.api.health.r4.api.DataAbsentReason.Reason.unsupported);
        }
      } catch (NoSuchFieldException e) {
        // Do nothing
      }
      return null;
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

      if (IsReference.class.isAssignableFrom(writer.getType().getRawClass())) {
        IsReference reference = (IsReference) ((BeanPropertyWriter) writer).get(pojo);
        include = config.isEnabled(reference);
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
      Object dar = dataAbsentReasonForExtensionField(pojo, extensionField);
      if (dar != null) {
        jgen.writeObjectField(extensionField, dar);
      } else if (!jgen.canOmitFields()) {
        writer.serializeAsOmittedField(pojo, jgen, provider);
      }
    }
  }

  /**
   * This serializer is fired for references _in_ a list. The {@link OptionalReferencesFilter} is
   * responsible for making sure the field references are omitted.
   */
  @RequiredArgsConstructor
  private final class OptionalReferenceSerializer<T extends IsReference> extends JsonSerializer<T> {
    /**
     * This is the default serializer used for references, we will delegate the hard parts to it.
     */
    private final JsonSerializer<T> delegate;

    /**
     * If the resource reference is well formed, extract the name, and check if it is an enabled
     * reference. Otherwise if it is malformed, always use default serialization.
     */
    @Override
    @SneakyThrows
    public void serialize(T value, JsonGenerator jgen, SerializerProvider provider) {
      if (value == null) {
        return;
      }
      if (config.isEnabled(value)) {
        delegate.serialize(value, jgen, provider);
      }
    }
  }
}
