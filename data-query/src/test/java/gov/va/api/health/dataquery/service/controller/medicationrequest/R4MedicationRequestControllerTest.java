package gov.va.api.health.dataquery.service.controller.medicationrequest;

import static gov.va.api.health.dataquery.service.controller.MockRequests.requestFromUri;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.ConfigurableBaseUrlPageLinks;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderEntity;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderRepository;
import gov.va.api.health.dataquery.service.controller.medicationorder.MedicationOrderSamples;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementEntity;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementRepository;
import gov.va.api.health.dataquery.service.controller.medicationstatement.MedicationStatementSamples;
import gov.va.api.health.ids.api.IdentityService;
import gov.va.api.lighthouse.vulcan.InvalidRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
public class R4MedicationRequestControllerTest {
  @Mock IdentityService ids;

  @Mock MedicationStatementRepository medicationStatementRepository;

  @Mock MedicationOrderRepository medicationOrderRepository;

  static Stream<Arguments> updateMedicationOrderCategory() {
    return Stream.of(
        arguments("123", null),
        arguments("123:P", null),
        arguments("123:O", "outpatient"),
        arguments("123:FP", "outpatient"),
        arguments("123:I", "inpatient"),
        arguments("123:FPI", "inpatient"));
  }

  R4MedicationRequestController _controller() {
    return new R4MedicationRequestController(
        new R4Bundler(new ConfigurableBaseUrlPageLinks("http://fonzy.com", "dstu2", "stu3", "r4")),
        LinkProperties.builder().defaultPageSize(15).build(),
        medicationOrderRepository,
        medicationStatementRepository,
        WitnessProtection.builder().identityService(ids).build(),
        ".*:(O|FP)",
        ".*:(I|FPI)");
  }

  @ParameterizedTest
  @ValueSource(strings = {"?nachos=friday", "?patient=p1&intent=ew-david"})
  @SneakyThrows
  void emptyBundle(String query) {
    var url = "http://fonzy.com/r4/MedicationRequest" + query;
    var request = requestFromUri(url);
    var bundle = _controller().search(request);
    assertThat(bundle.total()).isEqualTo(0);
    assertThat(bundle.entry()).isEmpty();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"?patient=p1&_id=123", "?patient=p1&identifier=123", "?_id=123&identifier=456"})
  @SneakyThrows
  void invalidRequests(String query) {
    var request = requestFromUri("http://fonzy.com/r4/MedicationRequest" + query);
    assertThatExceptionOfType(InvalidRequest.class).isThrownBy(() -> _controller().search(request));
  }

  @Test
  public void numberOfRequestsIsStatementsAndOrdersAdded() {
    when(ids.register(any()))
        .thenReturn(
            List.of(
                MedicationOrderSamples.registration("mo1", "pmr1"),
                MedicationOrderSamples.registration("mo2", "pmr2"),
                MedicationStatementSamples.registration("ms1", "pmr3")));
    var modm = MedicationOrderSamples.Datamart.create();
    var msdm = MedicationStatementSamples.Datamart.create();
    when(medicationOrderRepository.count(any(Specification.class))).thenReturn(2L);
    when(medicationStatementRepository.count(any(Specification.class))).thenReturn(1L);
    when(medicationOrderRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(modm.entity("mo1", "p1"), modm.entity("mo2", "p1")),
                    i.getArgument(1, Pageable.class),
                    2));
    when(medicationStatementRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(msdm.entity("ms1", "p1")), i.getArgument(1, Pageable.class), 2));
    // Medication Statements
    var request = requestFromUri("http://fonzy.com/r4/MedicationRequest?patient=p1");
    var actual = _controller().search(request);
    assertThat(actual.total()).isEqualTo(3);
    assertThat(actual.entry().stream().map(e -> e.resource().id())).containsExactly("pmr3");
    // Medication Orders
    request = requestFromUri("http://fonzy.com/r4/MedicationRequest?patient=p1&page=2");
    actual = _controller().search(request);
    assertThat(actual.total()).isEqualTo(3);
    assertThat(actual.entry().stream().map(e -> e.resource().id()))
        .containsExactlyInAnyOrder("pmr1", "pmr2");
  }

  @Test
  public void readMedOrder() {
    when(ids.register(any()))
        .thenReturn(List.of(MedicationOrderSamples.registration("mo1", "pmo1")));
    when(ids.lookup("pmo1")).thenReturn(List.of(MedicationOrderSamples.id("mo1")));
    var entity = MedicationOrderSamples.Datamart.create().entity("mo1", "p1");
    when(medicationOrderRepository.findById("mo1")).thenReturn(Optional.of(entity));
    assertThat(_controller().read("pmo1"))
        .isEqualTo(
            MedicationRequestSamples.R4
                .create()
                .medicationRequestFromMedicationOrder("pmo1", "p1"));
  }

  @Test
  public void readMedStatement() {
    when(ids.register(any()))
        .thenReturn(List.of(MedicationStatementSamples.registration("ms1", "pms1")));
    when(ids.lookup("pms1")).thenReturn(List.of(MedicationStatementSamples.id("ms1")));
    var entity = MedicationStatementSamples.Datamart.create().entity("ms1", "p1");
    when(medicationStatementRepository.findById("ms1")).thenReturn(Optional.of(entity));
    assertThat(_controller().read("pms1"))
        .isEqualTo(
            MedicationRequestSamples.R4
                .create()
                .medicationRequestFromMedicationStatement("pms1", "p1"));
  }

  @Test
  public void readRawMedOrderTest() {
    when(ids.lookup("pmo1")).thenReturn(List.of(MedicationOrderSamples.id("mo1")));
    var entity = MedicationOrderEntity.builder().cdwId("mo1").icn("p1").payload("payload").build();
    when(medicationOrderRepository.findById("mo1")).thenReturn(Optional.of(entity));
    assertThat(_controller().readRaw("pmo1", mock(HttpServletResponse.class))).isEqualTo("payload");
  }

  @Test
  public void readRawStatementTest() {
    when(ids.lookup("pms1")).thenReturn(List.of(MedicationStatementSamples.id("ms1")));
    var entity =
        MedicationStatementEntity.builder().cdwId("ms1").icn("p1").payload("payload").build();
    when(medicationStatementRepository.findById("ms1")).thenReturn(Optional.of(entity));
    assertThat(_controller().readRaw("pms1", mock(HttpServletResponse.class))).isEqualTo("payload");
  }

  @ParameterizedTest
  @MethodSource
  void updateMedicationOrderCategory(String cdwId, String expectedCategory) {
    when(ids.register(any()))
        .thenReturn(List.of(MedicationOrderSamples.registration(cdwId, "pmo1")));
    when(ids.lookup("pmo1")).thenReturn(List.of(MedicationOrderSamples.id(cdwId)));
    var entity = MedicationOrderSamples.Datamart.create().entity(cdwId, "p1");
    when(medicationOrderRepository.findById(cdwId)).thenReturn(Optional.of(entity));
    assertThat(
            Optional.ofNullable(_controller().read("pmo1").category())
                .map(c -> c.get(0).coding().get(0).code())
                .orElse(null))
        .isEqualTo(expectedCategory);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "?_id=pmr1",
        "?identifier=pmr1",
        "?patient=p1",
        "?patient=p1&intent=order",
        "?patient=p1&intent=plan"
      })
  void validRequests(String query) {
    lenient()
        .when(ids.register(any()))
        .thenReturn(
            List.of(
                MedicationOrderSamples.registration("mo1", "pmr1"),
                MedicationOrderSamples.registration("mo2", "pmr2"),
                MedicationStatementSamples.registration("ms1", "pmr3"),
                MedicationStatementSamples.registration("ms2", "pmr4")));
    lenient().when(ids.lookup("pmr1")).thenReturn(List.of(MedicationOrderSamples.id("mo1")));
    var modm = MedicationOrderSamples.Datamart.create();
    var msdm = MedicationStatementSamples.Datamart.create();
    lenient().when(medicationOrderRepository.count(any(Specification.class))).thenReturn(2L);
    lenient().when(medicationStatementRepository.count(any(Specification.class))).thenReturn(2L);
    lenient()
        .when(medicationOrderRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(modm.entity("mo1", "p1"), modm.entity("mo2", "p1")),
                    i.getArgument(1, Pageable.class),
                    2));
    lenient()
        .when(medicationStatementRepository.findAll(any(Specification.class), any(Pageable.class)))
        .thenAnswer(
            i ->
                new PageImpl(
                    List.of(msdm.entity("ms1", "p1"), msdm.entity("ms2", "p1")),
                    i.getArgument(1, Pageable.class),
                    2));
    var request = requestFromUri("http://fonzy.com/r4/MedicationRequest" + query);
    var actual = _controller().search(request);
    assertThat(actual.entry()).hasSize(2);
  }
}
