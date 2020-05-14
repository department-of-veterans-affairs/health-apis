package gov.va.api.health.dataquery.service.controller.patient;

import static gov.va.api.health.autoconfig.logging.LogSanitizer.sanitize;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.uscorer4.api.resources.Patient;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Patient Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-patient.html for
 * implementation details.
 */
@Slf4j
@Validated
@RestController
@SuppressWarnings("WeakerAccess")
@RequestMapping(
    value = {"/r4/Patient"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class R4PatientController {
  private R4Bundler bundler;

  private PatientRepositoryV2 repository;

  private WitnessProtection witnessProtection;

  /** Autowired constructor. */
  public R4PatientController(
      @Autowired R4Bundler bundler,
      @Autowired PatientRepositoryV2 repository,
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
    return bundler.bundle(linkConfig, reports, Patient.Entry::new, Patient.Bundle::new);
  }

  Patient.Bundle bundle(
      MultiValueMap<String, String> parameters, Page<PatientEntityV2> entitiesPage) {
    log.info("Search {} found {} results", parameters, entitiesPage.getTotalElements());
    if (Parameters.countOf(parameters) == 0) {
      return R4PatientController.this.bundle(
          parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartPatient> datamarts =
        entitiesPage.stream().map(PatientEntityV2::asDatamartPatient).collect(Collectors.toList());
    List<Patient> fhir =
        datamarts.stream()
            .map(dm -> R4PatientTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return R4PatientController.this.bundle(parameters, fhir, (int) entitiesPage.getTotalElements());
  }

  String cdwGender(String fhirGender) {
    String cdw = GenderMapping.toCdw(fhirGender);
    if (cdw == null) {
      throw new IllegalArgumentException("unknown gender: " + fhirGender);
    }
    return cdw;
  }

  PatientEntityV2 findById(String publicId) {
    Optional<PatientEntityV2> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count, PatientEntityV2.naturalOrder());
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Patient read(@PathVariable("publicId") String publicId) {
    DatamartPatient dm = findById(publicId).asDatamartPatient();
    return R4PatientTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    PatientEntityV2 entityV2 = findById(publicId);
    IncludesIcnMajig.addHeader(response, entityV2.icn());
    return entityV2.payload();
  }

  /** Search by Birthdate+Family. */
  @GetMapping(params = {"birthdate", "family"})
  public Patient.Bundle searchByBirthdateandFamily(
      @RequestParam("birthdate") String[] birthdate,
      @RequestParam("family") String family,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    PatientRepositoryV2.BirthdateAndFamilySpecification spec =
        PatientRepositoryV2.BirthdateAndFamilySpecification.builder()
            .family(family)
            .dates(birthdate)
            .build();
    log.info("PatientV2: Looking for {} {}", spec, sanitize(family));
    Page<PatientEntityV2> entities = repository.findAll(spec, page(page, count));
    return bundle(
        Parameters.builder()
            .add("family", family)
            .addAll("birthdate", birthdate)
            .add("page", page)
            .add("_count", count)
            .build(),
        entities);
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

  /** Search by Name. */
  @GetMapping(params = {"name"})
  public Patient.Bundle searchByName(
      @RequestParam("name") String name,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return bundle(
        Parameters.builder().add("name", name).add("page", page).add("_count", count).build(),
        repository.findByFullName(name, page(page, count)));
  }

  /** Search by Name+Birthdate. */
  @GetMapping(params = {"name", "birthdate"})
  public Patient.Bundle searchByNameAndBirthdate(
      @RequestParam("name") String name,
      @RequestParam("birthdate") String[] birthdate,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    PatientRepositoryV2.NameAndBirthdateSpecification spec =
        PatientRepositoryV2.NameAndBirthdateSpecification.builder()
            .name(name)
            .dates(birthdate)
            .build();
    log.info("PatientV2: Looking for {} {}", sanitize(name), spec);
    Page<PatientEntityV2> entities = repository.findAll(spec, page(page, count));
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
        repository.findByFullNameAndGender(name, cdwGender(gender), page(page, count)));
  }
}
