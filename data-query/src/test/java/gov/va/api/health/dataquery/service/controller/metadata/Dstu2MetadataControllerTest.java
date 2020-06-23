package gov.va.api.health.dataquery.service.controller.metadata;

import static gov.va.api.health.dataquery.service.controller.metadata.MetadataSamples.conformanceStatementProperties;
import static gov.va.api.health.dataquery.service.controller.metadata.MetadataSamples.pretty;
import static gov.va.api.health.dataquery.service.controller.metadata.MetadataSamples.referenceSerializerProperties;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.config.ReferenceSerializerProperties;
import gov.va.api.health.dataquery.service.controller.metadata.MetadataProperties.StatementType;
import gov.va.api.health.dstu2.api.resources.Conformance;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

public class Dstu2MetadataControllerTest {

  @Test
  @SneakyThrows
  public void readClinician() {
    MetadataProperties metadataProperties = conformanceStatementProperties();
    ReferenceSerializerProperties referenceSerializerProperties =
        referenceSerializerProperties(true);
    metadataProperties.setStatementType(StatementType.CLINICIAN);
    Dstu2MetadataController controller =
        new Dstu2MetadataController(metadataProperties, referenceSerializerProperties);
    Conformance old =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/clinician-conformance.json"), Conformance.class);
    assertThat(pretty(controller.read())).isEqualTo(pretty(old));
  }

  @Test
  @SneakyThrows
  public void readLab() {
    MetadataProperties metadataProperties = conformanceStatementProperties();
    ReferenceSerializerProperties referenceSerializerProperties =
        referenceSerializerProperties(false);
    metadataProperties.setStatementType(StatementType.PATIENT);
    Dstu2MetadataController controller =
        new Dstu2MetadataController(metadataProperties, referenceSerializerProperties);
    Conformance old =
        JacksonConfig.createMapper()
            .readValue(getClass().getResourceAsStream("/lab-conformance.json"), Conformance.class);
    try {
      assertThat(pretty(controller.read())).isEqualTo(pretty(old));
    } catch (AssertionError e) {
      System.out.println(e.getMessage());
      throw e;
    }
  }

  @Test
  @SneakyThrows
  public void readPatient() {
    MetadataProperties metadataProperties = conformanceStatementProperties();
    ReferenceSerializerProperties referenceSerializerProperties =
        referenceSerializerProperties(true);
    metadataProperties.setStatementType(StatementType.PATIENT);
    Dstu2MetadataController controller =
        new Dstu2MetadataController(metadataProperties, referenceSerializerProperties);
    Conformance old =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/patient-conformance.json"), Conformance.class);
    try {
      assertThat(pretty(controller.read())).isEqualTo(pretty(old));
    } catch (AssertionError e) {
      System.out.println(e.getMessage());
      throw e;
    }
  }
}
