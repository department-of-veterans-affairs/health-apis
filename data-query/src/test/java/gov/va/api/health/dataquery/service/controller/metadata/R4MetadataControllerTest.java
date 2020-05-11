package gov.va.api.health.dataquery.service.controller.metadata;

import static gov.va.api.health.dataquery.service.controller.metadata.MetadataSamples.conformanceStatementProperties;
import static gov.va.api.health.dataquery.service.controller.metadata.MetadataSamples.pretty;
import static gov.va.api.health.dataquery.service.controller.metadata.MetadataSamples.referenceSerializerProperties;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.config.ReferenceSerializerProperties;
import gov.va.api.health.dataquery.service.controller.metadata.MetadataProperties.StatementType;
import gov.va.api.health.stu3.api.resources.CapabilityStatement;
import lombok.SneakyThrows;
import org.junit.Test;

public class R4MetadataControllerTest {

  @Test
  @SneakyThrows
  public void readPatient() {
    MetadataProperties metadataProperties = conformanceStatementProperties();
    ReferenceSerializerProperties referenceSerializerProperties =
        referenceSerializerProperties(true);
    metadataProperties.setStatementType(StatementType.PATIENT);
    R4MetadataController controller =
        new R4MetadataController(metadataProperties, referenceSerializerProperties);
    CapabilityStatement old =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/patient-r4-capability.json"),
                CapabilityStatement.class);
    try {
      assertThat(pretty(controller.read())).isEqualTo(pretty(old));
    } catch (AssertionError e) {
      System.out.println(e.getMessage());
      throw e;
    }
  }
}
