package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Patient;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@Execution(ExecutionMode.CONCURRENT)
@ExtendWith(MockitoExtension.class)
public class R4PatientControllerTest {
  private static final String BASE_URL = "http://fonzy.com/r4";

  @Mock IdentityService ids;

  @Mock PatientRepositoryV2 repository;

  R4PatientController controller() {
    return new R4PatientController(
        WitnessProtection.builder().identityService(ids).build(),
        repository,
        LinkProperties.builder()
            .publicUrl("http://fonzy.com")
            .publicR4BasePath("r4")
            .maxPageSize(20)
            .defaultPageSize(15)
            .build());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?nachos=friday",
        "?given=Lucifer",
        "?_id=http://va.gov/mpi|",
        "?_id=|p1",
        "?identifier=http://va.gov/mpi|",
        "?identifier=|p1",
        "?gender=|other&family=Morningstar"
      })
  @SneakyThrows
  void emptyBundles(String query) {
    var request = requestFromUri(BASE_URL + "/Patient" + query);
    Patient.Bundle bundle = controller().search(request);
    assertThat(bundle.total()).isEqualTo(0);
    assertThat(bundle.entry()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=123&identifier=456",
        "?birthdate=lt2000",
        "?gender=other",
        "?name=Lucifer Morningstar&family=Morningstar"
      })
  @SneakyThrows
  void invalidRequests(String query) {
    var request = requestFromUri(BASE_URL + "/Patient" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(request));
  }

  @Test
  void read() {
    // No IDS Necessary
    PatientEntityV2 entity = PatientSamples.Datamart.create().entity("p1");
    when(repository.findById("p1")).thenReturn(Optional.of(entity));
    assertThat(controller().read("p1")).isEqualTo(PatientSamples.R4.create().patient("p1"));
  }

  @Test
  void readRaw() {
    // No IDS Necessary
    HttpServletResponse response = mock(HttpServletResponse.class);
    PatientEntityV2 entity = PatientEntityV2.builder().icn("p1").payload("payload!").build();
    when(repository.findById("p1")).thenReturn(Optional.of(entity));
    assertThat(controller().readRaw("p1", response)).isEqualTo("payload!");
  }

  @Test
  void toBundle() {
    // No IDS Necessary
    var vr =
        VulcanResult.<PatientEntityV2>builder()
            .paging(paging(BASE_URL + "/Patient?_id=p1&page=%d&_count=%d", 1, 4, 5, 6, 9, 15))
            .entities(Stream.of(PatientSamples.Datamart.create().entity("p1")))
            .build();
    var r4 = PatientSamples.R4.create().patient("p1");
    var expected =
        PatientSamples.R4.asBundle(
            BASE_URL,
            List.of(r4),
            999,
            PatientSamples.R4.link(
                BundleLink.LinkRelation.first, BASE_URL + "/Patient?_id=p1", 1, 15),
            PatientSamples.R4.link(
                BundleLink.LinkRelation.prev, BASE_URL + "/Patient?_id=p1", 4, 15),
            PatientSamples.R4.link(
                BundleLink.LinkRelation.self, BASE_URL + "/Patient?_id=p1", 5, 15),
            PatientSamples.R4.link(
                BundleLink.LinkRelation.next, BASE_URL + "/Patient?_id=p1", 6, 15),
            PatientSamples.R4.link(
                BundleLink.LinkRelation.last, BASE_URL + "/Patient?_id=p1", 9, 15));
    assertThat(controller().toBundle().apply(vr)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=p1",
        "?_id=http://va.gov/mpi|p1",
        "?identifier=p1",
        "?identifier=http://va.gov/mpi|p1",
        "?name=Lucifer Morningstar",
        "?birthdate=lt2000&name=Lucifer Morningstar",
        "?gender=male&name=Lucifer Morningstar",
        "?birthdate=lt2000&family=Morningstar",
        "?family=Morningstar&gender=male",
        "?family=Morningstar&gender=http://hl7.org/fhir/administrative-gender|male",
        "?family=Morningstar&gender=http://hl7.org/fhir/administrative-gender|"
      })
  @SneakyThrows
  void validRequests(String query) {
    // No IDS Necessary
    var entity = PatientSamples.Datamart.create().entity("p1");
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(i -> new PageImpl(List.of(entity), i.getArgument(1, Pageable.class), 1));
    var request = requestFromUri(BASE_URL + "/Patient" + query);
    var actual = controller().search(request);
    assertThat(actual.entry()).hasSize(1);
  }
}
