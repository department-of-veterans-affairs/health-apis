package gov.va.api.health.dataquery.service.controller.procedure;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.argonaut.api.resources.Procedure.Bundle;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.bundle.AbstractBundle.BundleType;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.Search;
import gov.va.api.health.dstu2.api.bundle.AbstractEntry.SearchMode;
import gov.va.api.health.dstu2.api.bundle.BundleLink;
import gov.va.api.health.dstu2.api.bundle.BundleLink.LinkRelation;
import gov.va.api.health.dstu2.api.datatypes.CodeableConcept;
import gov.va.api.health.dstu2.api.datatypes.Coding;
import gov.va.api.health.dstu2.api.elements.Reference;
import gov.va.dvp.cdw.xsd.model.CdwProcedure101Root;
import gov.va.dvp.cdw.xsd.model.CdwProcedure101Root.CdwProcedures;
import gov.va.dvp.cdw.xsd.model.CdwProcedure101Root.CdwProcedures.CdwProcedure;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
import javax.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.util.MultiValueMap;

public class ProcedureControllerTest {

  @Mock MrAndersonClient client;

  @Mock ProcedureController.Transformer tx;

  @Mock Bundler bundler;

  ProcedureController controller;

  @Before
  public void _init() {
    MockitoAnnotations.initMocks(this);
    controller =
        ProcedureController.builder()
            .transformer(tx)
            .mrAndersonClient(client)
            .bundler(bundler)
            .build();
  }

  private void assertSearch(
      Supplier<Procedure.Bundle> invocation, MultiValueMap<String, String> params) {
    CdwProcedure101Root root = new CdwProcedure101Root();
    root.setPageNumber(1);
    root.setRecordsPerPage(10);
    root.setRecordCount(3);
    root.setProcedures(new CdwProcedures());
    CdwProcedure cdwItem1 = new CdwProcedure();
    CdwProcedure cdwItem2 = new CdwProcedure();
    CdwProcedure cdwItem3 = new CdwProcedure();
    root.getProcedures().getProcedure().addAll(Arrays.asList(cdwItem1, cdwItem2, cdwItem3));
    Procedure procedure1 = Procedure.builder().build();
    Procedure procedure2 = Procedure.builder().build();
    Procedure procedure3 = Procedure.builder().build();
    when(tx.apply(cdwItem1)).thenReturn(procedure1);
    when(tx.apply(cdwItem2)).thenReturn(procedure2);
    when(tx.apply(cdwItem3)).thenReturn(procedure3);
    when(client.search(any())).thenReturn(root);
    Procedure.Bundle mockBundle = new Procedure.Bundle();
    when(bundler.bundle(any())).thenReturn(mockBundle);
    Procedure.Bundle actual = invocation.get();
    assertThat(actual).isSameAs(mockBundle);
    @SuppressWarnings("unchecked")
    ArgumentCaptor<BundleContext<CdwProcedure, Procedure, Procedure.Entry, Procedure.Bundle>>
        captor = ArgumentCaptor.forClass(BundleContext.class);
    verify(bundler).bundle(captor.capture());
    LinkConfig expectedLinkConfig =
        LinkConfig.builder()
            .page(1)
            .recordsPerPage(10)
            .totalRecords(3)
            .path("Procedure")
            .queryParams(params)
            .build();
    assertThat(captor.getValue().linkConfig()).isEqualTo(expectedLinkConfig);
    assertThat(captor.getValue().xmlItems()).isEqualTo(root.getProcedures().getProcedure());
    assertThat(captor.getValue().newBundle().get()).isInstanceOf(Procedure.Bundle.class);
    assertThat(captor.getValue().newEntry().get()).isInstanceOf(Procedure.Entry.class);
    assertThat(captor.getValue().transformer()).isSameAs(tx);
  }

  private Bundle bundleForPatient(String patientId, String patientDisplay) {
    return Procedure.Bundle.builder()
        .resourceType("Bundle")
        .type(BundleType.searchset)
        .total(1)
        .link(
            asList(
                BundleLink.builder()
                    .relation(LinkRelation.self)
                    .url(
                        "https://dev-api.va.gov/services/argonaut/v0/Procedure?patient="
                            + patientId
                            + "&page=1&_count=15")
                    .build(),
                BundleLink.builder()
                    .relation(LinkRelation.first)
                    .url(
                        "https://dev-api.va.gov/services/argonaut/v0/Procedure?patient="
                            + patientId
                            + "&page=1&_count=15")
                    .build(),
                BundleLink.builder()
                    .relation(LinkRelation.last)
                    .url(
                        "https://dev-api.va.gov/services/argonaut/v0/Procedure?patient="
                            + patientId
                            + "&page=1&_count=15")
                    .build()))
        .entry(
            asList(
                Procedure.Entry.builder()
                    .fullUrl(
                        "https://dev-api.va.gov/services/argonaut/v0/Procedure/532070f1-cb7b-582e-9380-9e0ef27bc817")
                    .resource(
                        Procedure.builder()
                            .resourceType("Procedure")
                            .id("532070f1-cb7b-582e-9380-9e0ef27bc817")
                            .subject(
                                Reference.builder()
                                    .reference(
                                        "https://dev-api.va.gov/services/argonaut/v0/Patient/"
                                            + patientId)
                                    .display(patientDisplay)
                                    .build())
                            .status(Procedure.Status.completed)
                            .code(
                                CodeableConcept.builder()
                                    .coding(
                                        asList(
                                            Coding.builder()
                                                .display("Documentation of current medications")
                                                .system("http://www.ama-assn.org/go/cpt")
                                                .code("XXXXX")
                                                .build()))
                                    .build())
                            .notPerformed(false)
                            .performedDateTime("2017-04-24T01:15:52Z")
                            .build())
                    .search(Search.builder().mode(SearchMode.match).build())
                    .build()))
        .build();
  }

  private Bundle bundleOf(Procedure resource) {
    return Bundle.builder()
        .type(BundleType.searchset)
        .resourceType("Bundle")
        .entry(
            Collections.singletonList(
                Procedure.Entry.builder().fullUrl("http://example.com").resource(resource).build()))
        .build();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void read() {
    CdwProcedure101Root root = new CdwProcedure101Root();
    root.setProcedures(new CdwProcedures());
    CdwProcedure xmlProcedure = new CdwProcedure();
    root.getProcedures().getProcedure().add(xmlProcedure);
    Procedure item = Procedure.builder().build();
    when(client.search(any())).thenReturn(root);
    when(tx.apply(xmlProcedure)).thenReturn(item);
    Procedure actual = controller.read("false", "hello", "");
    assertThat(actual).isSameAs(item);
    ArgumentCaptor<Query<CdwProcedure101Root>> captor = ArgumentCaptor.forClass(Query.class);
    verify(client).search(captor.capture());
    assertThat(captor.getValue().parameters()).isEqualTo(Parameters.forIdentity("hello"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void readHack() {
    String clarkKentId = "1938V0618";
    String clarkKentDisplay = "KENT,CLARK JOSEPH";
    String supermanId = "1938V0610";
    String supermanDisplay = "EL,KAL";
    controller =
        ProcedureController.builder()
            .transformer(tx)
            .mrAndersonClient(client)
            .bundler(bundler)
            .clarkKentId(clarkKentId)
            .clarkKentDisplay(clarkKentDisplay)
            .supermanId(supermanId)
            .supermanDisplay(supermanDisplay)
            .build();
    CdwProcedure101Root root = new CdwProcedure101Root();
    root.setProcedures(new CdwProcedures());
    CdwProcedure xmlProcedure = new CdwProcedure();
    root.getProcedures().getProcedure().add(xmlProcedure);
    when(client.search(any())).thenReturn(root);
    Procedure birdPlane =
        Procedure.builder()
            .subject(Reference.builder().id("1938V0618").display("KENT,CLARK JOSEPH").build())
            .build();
    Procedure superman =
        Procedure.builder()
            .subject(Reference.builder().id("1938V0610").display("EL,KAL").build())
            .build();
    when(tx.apply(xmlProcedure)).thenReturn(birdPlane);
    Procedure actual = controller.read("false", "hello", "1938V0610");
    assertThat(actual).isEqualTo(superman);
  }

  @Test
  public void searchById() {
    assertSearch(
        () -> controller.searchById("false", "me", "me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  public void searchByIdentifier() {
    assertSearch(
        () -> controller.searchByIdentifier("false", "me", "me", 1, 10),
        Parameters.builder().add("identifier", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  @SneakyThrows
  public void searchByPatientAndDateHack() {
    String clarkKentId = "1938V0618";
    String clarkKentDisplay = "KENT,CLARK JOSEPH";
    String supermanId = "1938V0610";
    String supermanDisplay = "EL,KAL";
    controller =
        ProcedureController.builder()
            .transformer(tx)
            .mrAndersonClient(client)
            .bundler(bundler)
            .clarkKentId(clarkKentId)
            .clarkKentDisplay(clarkKentDisplay)
            .supermanId(supermanId)
            .supermanDisplay(supermanDisplay)
            .build();
    when(client.search(any())).thenReturn(new CdwProcedure101Root());
    when(bundler.bundle(any())).thenReturn(bundleForPatient(clarkKentId, clarkKentDisplay));
    assertThat(
            controller.searchByPatientAndDate(
                "false", supermanId, new String[] {"2005", "2006"}, 1, 10))
        .isEqualTo(bundleForPatient(supermanId, supermanDisplay));
  }

  @Test
  public void searchByPatientAndDateRange() {
    assertSearch(
        () ->
            controller.searchByPatientAndDate("false", "me", new String[] {"2005", "2006"}, 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .addAll("date", "2005", "2006")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  @SneakyThrows
  public void searchByPatientHack() {
    String clarkKentId = "1938V0618";
    String clarkKentDisplay = "KENT,CLARK JOSEPH";
    String supermanId = "1938V0610";
    String supermanDisplay = "EL,KAL";
    controller =
        ProcedureController.builder()
            .transformer(tx)
            .mrAndersonClient(client)
            .bundler(bundler)
            .clarkKentId(clarkKentId)
            .clarkKentDisplay(clarkKentDisplay)
            .supermanId(supermanId)
            .supermanDisplay(supermanDisplay)
            .build();
    when(client.search(any())).thenReturn(new CdwProcedure101Root());
    when(bundler.bundle(any())).thenReturn(bundleForPatient(clarkKentId, clarkKentDisplay));
    assertThat(controller.searchByPatientAndDate("false", supermanId, null, 1, 10))
        .isEqualTo(bundleForPatient(supermanId, supermanDisplay));
  }

  @Test
  public void searchByPatientandDate() {
    assertSearch(
        () -> controller.searchByPatientAndDate("false", "me", new String[] {"2000"}, 1, 10),
        Parameters.builder()
            .add("patient", "me")
            .addAll("date", "2000")
            .add("page", 1)
            .add("_count", 10)
            .build());
  }

  @Test
  public void searchByPatientandNoDate() {
    assertSearch(
        () -> controller.searchByPatientAndDate("false", "me", null, 1, 10),
        Parameters.builder().add("patient", "me").add("page", 1).add("_count", 10).build());
  }

  @Test
  @SneakyThrows
  public void validateAcceptsValidBundle() {
    Procedure resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-procedure-1.01.json"), Procedure.class);
    Procedure.Bundle bundle = bundleOf(resource);
    assertThat(controller.validate(bundle)).isEqualTo(Validator.ok());
  }

  @SneakyThrows
  @Test(expected = ConstraintViolationException.class)
  public void validateThrowsExceptionForInvalidBundle() {
    Procedure resource =
        JacksonConfig.createMapper()
            .readValue(
                getClass().getResourceAsStream("/cdw/old-procedure-1.01.json"), Procedure.class);
    resource.resourceType(null);
    Procedure.Bundle bundle = bundleOf(resource);
    controller.validate(bundle);
  }
}
