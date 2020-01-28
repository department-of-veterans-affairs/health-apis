package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.Patient;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@Slf4j
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/dstu2/Patient"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class Dstu2PatientController {

  private Dstu2Bundler bundler;

  private PatientSearchRepository repository;

  private WitnessProtection witnessProtection;

  /** Autowired constructor. */
  public Dstu2PatientController(
      @Autowired Dstu2Bundler bundler,
      @Autowired PatientSearchRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  Patient.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Patient> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Patient")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig, reports, Patient.Entry::new, Patient.Bundle::new));
  }

  Patient.Bundle bundle(
      MultiValueMap<String, String> parameters, Page<PatientSearchEntity> entities) {
    log.info("Search {} found {} results", parameters, entities.getTotalElements());
    if (Parameters.countOf(parameters) == 0) {
      return bundle(parameters, emptyList(), (int) entities.getTotalElements());
    }
    return bundle(
        parameters,
        entities
            .get()
            .map(PatientSearchEntity::asDatamartPatient)
            .map(this::transform)
            .collect(Collectors.toList()),
        (int) entities.getTotalElements());
  }

  String cdwGender(String fhirGender) {
    String cdw = GenderMapping.toCdw(fhirGender);
    if (cdw == null) {
      throw new IllegalArgumentException("unknown gender: " + fhirGender);
    }
    return cdw;
  }

  PatientSearchEntity findById(String publicId) {
    Optional<PatientSearchEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count, PatientSearchEntity.naturalOrder());
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Patient read(@PathVariable("publicId") String publicId) {
    DatamartPatient dm = findById(publicId).asDatamartPatient();
    return Dstu2PatientTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    PatientSearchEntity entity = findById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.patient().payload();
  }

  /** Search by Family+Gender. */
  @GetMapping(params = {"family", "gender"})
  public Patient.Bundle searchByFamilyAndGender(
      @RequestParam("family") String family,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("family", family)
            .add("gender", gender)
            .add("page", page)
            .add("_count", count)
            .build(),
        repository.findByLastNameAndGender(family, cdwGender(gender), page(page, count)));
  }

  /** Search by Given+Gender. */
  @GetMapping(params = {"given", "gender"})
  public Patient.Bundle searchByGivenAndGender(
      @RequestParam("given") String given,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("given", given)
            .add("gender", gender)
            .add("page", page)
            .add("_count", count)
            .build(),
        repository.findByFirstNameAndGender(given, cdwGender(gender), page(page, count)));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Patient.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(id, page, count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Patient.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build();
    Patient resource = read(identifier);
    int totalRecords = resource == null ? 0 : 1;
    if (resource == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, asList(resource), totalRecords);
  }

  /** Search by Name+Birthdate. */
  @GetMapping(params = {"name", "birthdate"})
  public Patient.Bundle searchByNameAndBirthdate(
      @RequestParam("name") String name,
      @RequestParam("birthdate") String[] birthdate,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    PatientSearchRepository.NameAndBirthdateSpecification spec =
        PatientSearchRepository.NameAndBirthdateSpecification.builder()
            .name(name)
            .dates(birthdate)
            .build();

    log.info("Looking for {} {}", sanitize(name), spec);
    Page<PatientSearchEntity> entities = repository.findAll(spec, page(page, count));
    return bundle(
        Parameters.builder()
            .add("name", name)
            .addAll("birthdate", birthdate)
            .add("page", page)
            .add("_count", count)
            .build(),
        entities);
  }

  /** Search by Name+Gender. */
  @GetMapping(params = {"name", "gender"})
  public Patient.Bundle searchByNameAndGender(
      @RequestParam("name") String name,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return bundle(
        Parameters.builder()
            .add("name", name)
            .add("gender", gender)
            .add("page", page)
            .add("_count", count)
            .build(),
        repository.findByNameAndGender(name, cdwGender(gender), page(page, count)));
  }

  Patient transform(DatamartPatient dm) {
    return Dstu2PatientTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
      value = "/$validate",
      consumes = {"application/json", "application/json+fhir", "application/fhir+json"})
  public OperationOutcome validate(@RequestBody Patient.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }
}
