package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;

import gov.va.api.health.dataquery.api.resources.OperationOutcome;
import gov.va.api.health.dataquery.api.resources.Patient;
import gov.va.api.health.dataquery.api.resources.Patient.Bundle;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.Bundler.BundleContext;
import gov.va.api.health.dataquery.service.controller.PageLinks.LinkConfig;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root;
import gov.va.dvp.cdw.xsd.model.CdwPatient103Root.CdwPatients.CdwPatient;
import java.util.Collections;
import java.util.function.Function;
import javax.validation.constraints.Min;
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
 * Request Mappings for Patient Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-patient.html for implementation
 * details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"Patient", "/api/Patient"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class PatientController {
  private Transformer transformer;
  private MrAndersonClient mrAndersonClient;
  private Bundler bundler;

  private Bundle bundle(MultiValueMap<String, String> parameters, int page, int count) {
    CdwPatient103Root root = search(parameters);
    LinkConfig linkConfig =
        LinkConfig.builder()
            .path("Patient")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount())
            .build();
    return bundler.bundle(
        BundleContext.of(
            linkConfig,
            root.getPatients() == null ? Collections.emptyList() : root.getPatients().getPatient(),
            transformer,
            Patient.Entry::new,
            Patient.Bundle::new));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Patient read(@PathVariable("publicId") String publicId) {
    return transformer.apply(
        firstPayloadItem(
            hasPayload(search(Parameters.forIdentity(publicId)).getPatients()).getPatient()));
  }

  private CdwPatient103Root search(MultiValueMap<String, String> params) {
    Query<CdwPatient103Root> query =
        Query.forType(CdwPatient103Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("Patient")
            .version("1.03")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Search by Family+Gender. */
  @GetMapping(params = {"family", "gender"})
  public Patient.Bundle searchByFamilyAndGender(
      @RequestParam("family") String family,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("family", family)
            .add("gender", gender)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by Given+Gender. */
  @GetMapping(params = {"given", "gender"})
  public Patient.Bundle searchByGivenAndGender(
      @RequestParam("given") String given,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("given", given)
            .add("gender", gender)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Patient.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("_id", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Patient.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "1") @Min(0) int count) {
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by Name+Birthdate. */
  @GetMapping(params = {"name", "birthdate"})
  public Patient.Bundle searchByNameAndBirthdate(
      @RequestParam("name") String name,
      @RequestParam("birthdate") String[] birthdate,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("name", name)
            .addAll("birthdate", birthdate)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by Name+Gender. */
  @GetMapping(params = {"name", "gender"})
  public Patient.Bundle searchByNameAndGender(
      @RequestParam("name") String name,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @RequestParam(value = "_count", defaultValue = "15") @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("name", name)
            .addAll("gender", gender)
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
  public OperationOutcome validate(@RequestBody Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer extends Function<CdwPatient, Patient> {}
}
