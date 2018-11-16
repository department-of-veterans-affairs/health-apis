package gov.va.api.health.argonaut.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.va.api.health.argonaut.api.Patient.Bundle;
import gov.va.api.health.argonaut.api.Patient.Entry;
import gov.va.api.health.argonaut.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.argonaut.api.bundle.AbstractEntry;
import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.argonaut.api.samples.SampleDataTypes;
import gov.va.api.health.argonaut.api.samples.SamplePatients;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import java.util.Collections;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class PatientBundleTest {

  private SamplePatients patientData = SamplePatients.get();
  private SampleDataTypes dataTypes = SampleDataTypes.get();

  @Test
  public void bundlerCanBuildPatientBundles() {
    Entry entry =
        Entry.builder()
            .extension(Collections.singletonList(dataTypes.extension()))
            .fullUrl("http://patient.com")
            .id("123")
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(LinkRelation.self)
                        .url(("http://patient.com/1"))
                        .build()))
            .resource(patientData.alivePatient())
            .search(dataTypes.search())
            .request(dataTypes.request())
            .response(dataTypes.response())
            .build();

    Bundle bundle =
        Bundle.builder()
            .entry(Collections.singletonList(entry))
            .link(
                Collections.singletonList(
                    BundleLink.builder()
                        .relation(LinkRelation.self)
                        .url(("http://patient.com/2"))
                        .build()))
            .type(BundleType.searchset)
            .build();

    roundTrip(bundle);

    AbstractEntry.Search.builder().build().id();
  }

  @SneakyThrows
  private <T> T roundTrip(T object) {
    ObjectMapper mapper = new JacksonConfig().objectMapper();
    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    log.info("{}", json);
    Object evilTwin = mapper.readValue(json, object.getClass());
    assertThat(evilTwin).isEqualTo(object);
    return object;
  }
}
