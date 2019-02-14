package gov.va.api.health.argonaut.service.controller.procedure;

import static gov.va.api.health.argonaut.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.argonaut.service.controller.Transformers.hasPayload;

import gov.va.api.health.argonaut.api.bundle.BundleLink;
import gov.va.api.health.argonaut.api.elements.Reference;
import gov.va.api.health.argonaut.api.resources.OperationOutcome;
import gov.va.api.health.argonaut.api.resources.Procedure;
import gov.va.api.health.argonaut.api.resources.Procedure.Bundle;
import gov.va.api.health.argonaut.service.controller.Bundler;
import gov.va.api.health.argonaut.service.controller.Bundler.BundleContext;
import gov.va.api.health.argonaut.service.controller.DateTimeParameter;
import gov.va.api.health.argonaut.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.argonaut.service.controller.Parameters;
import gov.va.api.health.argonaut.service.controller.Validator;
import gov.va.api.health.argonaut.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.argonaut.service.mranderson.client.Query;
import gov.va.api.health.autoconfig.configuration.JacksonConfig;
import gov.va.dvp.cdw.xsd.model.CdwProcedure101Root;
import java.util.Collections;
import java.util.function.Function;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for the Argonaut Procedure Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-procedure.html for
 * implementation details.
 */
@Slf4j
@Validated
@RestController
@RequestMapping(
  value = {"/api/Procedure"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class ProcedureController {
  private static final String PLUTO_ID = "1011537977V693883";

  private static final String HAS_PROCEDURE_PATIENT_ID = "1008893838V179036";

  private static final String INTEGRATION_TEST_ID = "185601V825290";

  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Bundler bundler;

  private Procedure.Bundle bundle(MultiValueMap<String, String> parameters, int page, int count) {
    CdwProcedure101Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("Procedure")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getProcedures() == null
                ? Collections.emptyList()
                : root.getProcedures().getProcedure(),
            transformer,
            Procedure.Entry::new,
            Procedure.Bundle::new));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Procedure read(@PathVariable("publicId") String publicId) {
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getProcedures()).getProcedure()));
  }

  private CdwProcedure101Root search(MultiValueMap<String, String> params) {
    Query<CdwProcedure101Root> query =
        Query.forType(CdwProcedure101Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("Procedure")
            .version("1.01")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Procedure.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Procedure.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by patient. */
  @SneakyThrows
  @GetMapping(params = {"patient"})
  public Procedure.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    if (patient.equals(PLUTO_ID)) {
      return searchByPatientHack(page, count);
    }
    return bundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by patient and date. */
  @GetMapping(params = {"patient", "date"})
  public Procedure.Bundle searchByPatientAndDate(
      @RequestParam("patient") String patient,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    if (patient.equals(PLUTO_ID)) {
      return searchByPatientAndDateHack(date, page, count);
    }
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  @SneakyThrows
  private Procedure.Bundle searchByPatientAndDateHack(String[] date, int page, int count) {
    log.error(
        "Doing hack to replace pluto ID {} with integration test ID {}.",
        PLUTO_ID,
        INTEGRATION_TEST_ID);

    Bundle bundle =
        bundle(
            Parameters.builder()
                .add("patient", INTEGRATION_TEST_ID)
                .addAll("date", date)
                .add("page", page)
                .add("_count", count)
                .build(),
            page,
            count);

    for (BundleLink link : bundle.link()) {
      link.url(link.url().replaceAll(INTEGRATION_TEST_ID, PLUTO_ID));
    }
    for (Procedure.Entry entry : bundle.entry()) {
      Reference subject = entry.resource().subject();
      subject.reference(subject.reference().replaceAll(INTEGRATION_TEST_ID, PLUTO_ID));
    }
    log.error("Returned bundle is {}", JacksonConfig.createMapper().writeValueAsString(bundle));
    return bundle;
  }

  @SneakyThrows
  private Procedure.Bundle searchByPatientHack(int page, int count) {
    log.error(
        "Doing hack to replace pluto ID {} with integration test ID {}.",
        PLUTO_ID,
        INTEGRATION_TEST_ID);

    Procedure.Bundle bundle =
        bundle(
            Parameters.builder()
                .add("patient", INTEGRATION_TEST_ID)
                .add("page", page)
                .add("_count", count)
                .build(),
            page,
            count);

    for (BundleLink link : bundle.link()) {
      link.url(link.url().replaceAll(INTEGRATION_TEST_ID, PLUTO_ID));
    }
    for (Procedure.Entry entry : bundle.entry()) {
      Reference subject = entry.resource().subject();
      subject.reference(subject.reference().replaceAll(INTEGRATION_TEST_ID, PLUTO_ID));
    }
    log.error("Returned bundle is {}", JacksonConfig.createMapper().writeValueAsString(bundle));
    return bundle;
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Procedure.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<CdwProcedure101Root.CdwProcedures.CdwProcedure, Procedure> {}
}
