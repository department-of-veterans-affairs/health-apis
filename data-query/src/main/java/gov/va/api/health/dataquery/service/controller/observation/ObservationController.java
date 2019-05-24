package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;

import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root;
import java.util.Collections;
import java.util.function.Function;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
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
 * Request Mappings for Observation Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-Observation.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"Observation", "/api/Observation"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class ObservationController {
  private Transformer transformer;
  private MrAndersonClient mrAndersonClient;
  private Bundler bundler;

  private Observation.Bundle bundle(MultiValueMap<String, String> parameters, int page, int count) {
    CdwObservation104Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("Observation")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getObservations() == null
                ? Collections.emptyList()
                : root.getObservations().getObservation(),
            transformer,
            Observation.Entry::new,
            Observation.Bundle::new));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Observation read(@PathVariable("publicId") String publicId) {
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getObservations())
                .getObservation()));
  }

  private CdwObservation104Root search(MultiValueMap<String, String> params) {
    Query<CdwObservation104Root> query =
        Query.forType(CdwObservation104Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("Observation")
            .version("1.04")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Observation.Bundle searchById(
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
  public Observation.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public Observation.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by patient and category and date if available. */
  @GetMapping(params = {"patient", "category"})
  public Observation.Bundle searchByPatientAndCategory(
      @RequestParam("patient") String patient,
      @RequestParam("category") String category,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("category", category)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by patient and code. */
  @GetMapping(params = {"patient", "code"})
  public Observation.Bundle searchByPatientAndCode(
      @RequestParam("patient") String patient,
      @RequestParam("code") String code,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("code", code)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Observation.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<CdwObservation104Root.CdwObservations.CdwObservation, Observation> {}
}
