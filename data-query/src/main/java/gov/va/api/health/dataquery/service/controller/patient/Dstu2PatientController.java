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
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
  PatientV2 patientV2 = new PatientV2();

  private boolean defaultPatientV2;

  private Dstu2Bundler bundler;

  private PatientSearchRepository repository;

  private PatientRepositoryV2 repositoryV2;

  private WitnessProtection witnessProtection;

  /** Autowired constructor. */
  public Dstu2PatientController(
      @Value(value = "${default.patientV2}") boolean defaultPatientV2,
      @Autowired Dstu2Bundler bundler,
      @Autowired PatientSearchRepository repository,
      @Autowired PatientRepositoryV2 repositoryV2,
      @Autowired WitnessProtection witnessProtection) {
    this.defaultPatientV2 = defaultPatientV2;
    this.bundler = bundler;
    this.repository = repository;
    this.repositoryV2 = repositoryV2;
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
  public Patient read(
      @RequestHeader(name = "patientV2", required = false, defaultValue = "")
          String patientV2Header,
      @PathVariable("publicId") String publicId) {
    if (patientV2.isPatientV2Request(patientV2Header)) {
      log.info("Using PatientV2");
      DatamartPatient dm = patientV2.findById(publicId).asDatamartPatient();
      return Dstu2PatientTransformer.builder().datamart(dm).build().toFhir();
    }
    DatamartPatient dm = findById(publicId).asDatamartPatient();
    return Dstu2PatientTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(
      @RequestHeader(name = "patientV2", required = false, defaultValue = "")
          String patientV2Header,
      @PathVariable("publicId") String publicId,
      HttpServletResponse response) {
    if (patientV2.isPatientV2Request(patientV2Header)) {
      log.info("Using PatientV2");
      PatientEntityV2 entityV2 = patientV2.findById(publicId);
      IncludesIcnMajig.addHeader(response, entityV2.icn());
      return entityV2.payload();
    }
    PatientSearchEntity entity = findById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.patient().payload();
  }

  /** Search by Family+Gender. */
  @GetMapping(params = {"family", "gender"})
  public Patient.Bundle searchByFamilyAndGender(
      @RequestHeader(name = "patientV2", required = false, defaultValue = "")
          String patientV2Header,
      @RequestParam("family") String family,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (patientV2.isPatientV2Request(patientV2Header)) {
      return patientV2.bundle(
          Parameters.builder()
              .add("family", family)
              .add("gender", gender)
              .add("page", page)
              .add("_count", count)
              .build(),
          repositoryV2.findByLastNameAndGender(
              family, cdwGender(gender), patientV2.page(page, count)));
    }
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
      @RequestHeader(name = "patientV2", required = false, defaultValue = "")
          String patientV2Header,
      @RequestParam("given") String given,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (patientV2.isPatientV2Request(patientV2Header)) {
      return patientV2.bundle(
          Parameters.builder()
              .add("given", given)
              .add("gender", gender)
              .add("page", page)
              .add("_count", count)
              .build(),
          repositoryV2.findByFirstNameAndGender(
              given, cdwGender(gender), patientV2.page(page, count)));
    }
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
      @RequestHeader(name = "patientV2", required = false, defaultValue = "")
          String patientV2Header,
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(patientV2Header, id, page, count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Patient.Bundle searchByIdentifier(
      @RequestHeader(name = "patientV2", required = false, defaultValue = "")
          String patientV2Header,
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build();
    Patient resource = read(patientV2Header, identifier);
    int totalRecords = resource == null ? 0 : 1;
    if (resource == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, asList(resource), totalRecords);
  }

  /** Search by Name+Birthdate. */
  @GetMapping(params = {"name", "birthdate"})
  public Patient.Bundle searchByNameAndBirthdate(
      @RequestHeader(name = "patientV2", required = false, defaultValue = "")
          String patientV2Header,
      @RequestParam("name") String name,
      @RequestParam("birthdate") String[] birthdate,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (patientV2.isPatientV2Request(patientV2Header)) {
      PatientRepositoryV2.NameAndBirthdateSpecification spec =
          PatientRepositoryV2.NameAndBirthdateSpecification.builder()
              .name(name)
              .dates(birthdate)
              .build();
      log.info("PatientV2: Looking for {} {}", sanitize(name), spec);
      Page<PatientEntityV2> entities = repositoryV2.findAll(spec, page(page, count));
      return patientV2.bundle(
          Parameters.builder()
              .add("name", name)
              .addAll("birthdate", birthdate)
              .add("page", page)
              .add("_count", count)
              .build(),
          entities);
    }
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
      @RequestHeader(name = "patientV2", required = false, defaultValue = "")
          String patientV2Header,
      @RequestParam("name") String name,
      @RequestParam("gender") String gender,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (patientV2.isPatientV2Request(patientV2Header)) {
      return patientV2.bundle(
          Parameters.builder()
              .add("name", name)
              .add("gender", gender)
              .add("page", page)
              .add("_count", count)
              .build(),
          repositoryV2.findByFullNameAndGender(
              name, cdwGender(gender), patientV2.page(page, count)));
    }
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

  /** Container class for organizing patientv2 logic. */
  private class PatientV2 {
    Patient.Bundle bundle(
        MultiValueMap<String, String> parameters, Page<PatientEntityV2> entitiesPage) {
      log.info("Search {} found {} results", parameters, entitiesPage.getTotalElements());
      if (Parameters.countOf(parameters) == 0) {
        return Dstu2PatientController.this.bundle(
            parameters, emptyList(), (int) entitiesPage.getTotalElements());
      }
      List<DatamartPatient> datamarts =
          entitiesPage.stream()
              .map(PatientEntityV2::asDatamartPatient)
              .collect(Collectors.toList());
      List<Patient> fhir =
          datamarts.stream()
              .map(dm -> Dstu2PatientTransformer.builder().datamart(dm).build().toFhir())
              .collect(Collectors.toList());
      return Dstu2PatientController.this.bundle(
          parameters, fhir, (int) entitiesPage.getTotalElements());
    }

    PatientEntityV2 findById(String publicId) {
      Optional<PatientEntityV2> entity = repositoryV2.findById(witnessProtection.toCdwId(publicId));
      return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
    }

    boolean isPatientV2Request(String patientV2Header) {
      if (patientV2Header.isBlank()) {
        return defaultPatientV2;
      }
      return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(patientV2Header));
    }

    PageRequest page(int page, int count) {
      return PageRequest.of(page - 1, count == 0 ? 1 : count, PatientEntityV2.naturalOrder());
    }
  }
}
