package gov.va.api.health.dataquery.service.controller.condition;

import static gov.va.api.health.dataquery.service.controller.MockRequests.paging;
import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.id;
import static gov.va.api.health.dataquery.service.controller.condition.ConditionSamples.registration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.appointment.AppointmentSamples;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.health.r4.api.bundle.BundleLink;
import gov.va.api.health.r4.api.resources.Condition;
import gov.va.api.lighthouse.datamart.CompositeCdwId;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import gov.va.api.lighthouse.vulcan.VulcanResult;
import java.math.BigInteger;
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
public class R4ConditionControllerTest {
  private static final String BASE_URL = "http://fonzy.com/r4";

  @Mock IdentityService ids;

  @Mock ConditionRepository repository;

  R4ConditionController controller() {
    return new R4ConditionController(
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
        "?patient=p1&category=|encounter-diagnosis",
        "?patient=p1&category=http://terminology.hl7.org/CodeSystem/condition-category|nah",
        "?patient=p1&category=nah",
        "?patient=p1&category=http://nope.com|",
        "?patient=p1&category=http://nope.com|encounter-diagnosis",
        "?patient=p1&clinical-status=|active",
        "?patient=p1&clinical-status=nah",
        "?patient=p1&clinical-status=http://terminology.hl7.org/CodeSystem/condition-clinical|nah",
        "?patient=p1&clinical-status=http://nope.com|",
        "?patient=p1&clinical-status=http://nope.com|active"
      })
  @SneakyThrows
  void emptyBundle(String query) {
    var request = requestFromUri(BASE_URL + "/Condition" + query);
    Condition.Bundle bundle = controller().search(request);
    assertThat(bundle.total()).isEqualTo(0);
    assertThat(bundle.entry()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=123&identifier=456",
        "?patient=p1&_id=123",
        "?patient=p1&identifier=456",
        "?category=encounter-diagnosis",
        "?clinical-status=active"
      })
  @SneakyThrows
  void invalidRequests(String query) {
    var request = requestFromUri(BASE_URL + "/Condition" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> controller().search(request));
  }

  @Test
  void read() {
    when(ids.register(any())).thenReturn(List.of(registration("1:C", "pc1")));
    when(ids.lookup("pc1")).thenReturn(List.of(id("1:C")));
    ConditionEntity entity = ConditionSamples.Datamart.create().entity("1", "C", "p1");
    when(repository.findById(CompositeCdwId.fromCdwId("1:C"))).thenReturn(Optional.of(entity));
    assertThat(controller().read("pc1"))
        .isEqualTo(ConditionSamples.R4.create().condition("pc1", "p1"));
  }

  @Test
  void readRaw() {
    when(ids.lookup("pc1")).thenReturn(List.of(id("1:C")));
    ConditionEntity entity =
        ConditionEntity.builder()
            .cdwIdNumber(new BigInteger("1"))
            .cdwIdResourceCode('C')
            .icn("p1")
            .payload("payload")
            .build();
    when(repository.findById(CompositeCdwId.fromCdwId("1:C"))).thenReturn(Optional.of(entity));
    assertThat(controller().readRaw("pc1", mock(HttpServletResponse.class))).isEqualTo("payload");
  }

  @Test
  void toBundle() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("1:C", "pc1"),
                registration("2:C", "pc2"),
                registration("3:C", "pc3")));
    var bundler = controller().toBundle();
    ConditionSamples.Datamart dm = ConditionSamples.Datamart.create();
    var basePath = BASE_URL + "/Condition";
    var vr =
        VulcanResult.<ConditionEntity>builder()
            .paging(paging(basePath + "?patient=p1&page=%d&_count=%d", 1, 4, 5, 6, 9, 15))
            .entities(
                Stream.of(
                    dm.entity("1", "C", "p1"),
                    dm.entity("2", "C", "p1"),
                    dm.entity("3", "C", "p1")))
            .build();
    ConditionSamples.R4 r4 = ConditionSamples.R4.create();
    var expected =
        ConditionSamples.R4.asBundle(
            BASE_URL,
            List.of(
                r4.condition("pc1", "p1"), r4.condition("pc2", "p1"), r4.condition("pc3", "p1")),
            999,
            ConditionSamples.R4.link(
                BundleLink.LinkRelation.first, basePath + "?patient=p1", 1, 15),
            ConditionSamples.R4.link(BundleLink.LinkRelation.prev, basePath + "?patient=p1", 4, 15),
            ConditionSamples.R4.link(BundleLink.LinkRelation.self, basePath + "?patient=p1", 5, 15),
            ConditionSamples.R4.link(BundleLink.LinkRelation.next, basePath + "?patient=p1", 6, 15),
            ConditionSamples.R4.link(
                BundleLink.LinkRelation.last, basePath + "?patient=p1", 9, 15));
    assertThat(bundler.apply(vr)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=pc1",
        "?identifier=pc1",
        "?patient=p1",
        "?patient=p1&category=encounter-diagnosis",
        "?patient=p1&category=problem-list-item",
        "?patient=p1&category=http://terminology.hl7.org/CodeSystem/condition-category|",
        "?patient=p1&category=http://terminology.hl7.org/CodeSystem/condition-category|encounter-diagnosis",
        "?patient=p1&category=http://terminology.hl7.org/CodeSystem/condition-category|problem-list-item",
        "?patient=p1&clinical-status=active",
        "?patient=p1&clinical-status=resolved",
        "?patient=p1&clinical-status=inactive",
        "?patient=p1&clinical-status=http://terminology.hl7.org/CodeSystem/condition-clinical|",
        "?patient=p1&clinical-status=http://terminology.hl7.org/CodeSystem/condition-clinical|active",
        "?patient=p1&&clinical-status=http://terminology.hl7.org/CodeSystem/condition-clinical|resolved",
        "?patient=p1&clinical-status=http://terminology.hl7.org/CodeSystem/condition-clinical|inactive",
      })
  @SneakyThrows
  void validRequests(String query) {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                registration("1:C", "pc1"), registration("c2", "pc2"), registration("c3", "pc3")));
    // this is only really needed for ID searches
    lenient().when(ids.lookup(eq("pc1"))).thenReturn(List.of(AppointmentSamples.id("1:C")));

    ConditionSamples.Datamart dm = ConditionSamples.Datamart.create();
    when(repository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(
                        dm.entity("1", "C", "p1"),
                        dm.entity("2", "C", "p1"),
                        dm.entity("3", "C", "p1")),
                    i.getArgument(1, Pageable.class),
                    3));
    var request = requestFromUri(BASE_URL + "/Condition" + query);
    var actual = controller().search(request);
    assertThat(actual.entry()).hasSize(3);
  }
}
