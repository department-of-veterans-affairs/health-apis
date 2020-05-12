package gov.va.api.health.dataquery.service.controller.metadata;

import static gov.va.api.health.dataquery.service.controller.metadata.MetadataSamples.conformanceStatementProperties;
import static gov.va.api.health.dataquery.service.controller.metadata.MetadataSamples.pretty;
import static gov.va.api.health.dataquery.service.controller.metadata.MetadataSamples.referenceSerializerProperties;
import static org.assertj.core.api.Assertions.assertThat;

import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.config.ReferenceSerializerProperties;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.metadata.MetadataProperties.StatementType;
import gov.va.api.health.r4.api.resources.CapabilityStatement;
import java.util.Properties;
import lombok.SneakyThrows;
import org.junit.Test;
import org.springframework.boot.info.BuildProperties;

public class R4MetadataControllerTest {

  @Test
  @SneakyThrows
  public void readPatient() {
    MetadataProperties metadataProperties = conformanceStatementProperties();
    ReferenceSerializerProperties referenceSerializerProperties =
        referenceSerializerProperties(true);
    metadataProperties.setStatementType(StatementType.PATIENT);
    ConfigurableBaseUrlPageLinks pageLinks =
        ConfigurableBaseUrlPageLinks.builder()
            .baseUrl("http://awesome.com")
            .r4BasePath("r4")
            .build();
    Properties properties = new Properties();
    properties.setProperty("group", "gov.va");
    properties.setProperty("artifact", "data-query");
    properties.setProperty("version", "1.2.3");
    properties.setProperty("time", "2005-01-21T07:57:00Z");
    BuildProperties buildProperties = new BuildProperties(properties);
    R4MetadataController controller =
        new R4MetadataController(
            pageLinks, metadataProperties, referenceSerializerProperties, buildProperties);
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
