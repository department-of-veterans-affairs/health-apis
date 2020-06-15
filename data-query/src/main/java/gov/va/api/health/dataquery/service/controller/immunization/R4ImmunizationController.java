package gov.va.api.health.dataquery.service.controller.immunization;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.TokenParameter;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.uscorer4.api.resources.Immunization;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Immunization Profile, see
 * https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-immunization.html for implementation
 * details.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Immunization"},
    produces = {"application/json", "application/fhir+json"})
public class R4ImmunizationController {
  R4Bundler bundler;

  private ImmunizationRepository repository;

  private WitnessProtection witnessProtection;

  /** R4 Immunization Constructor. */
  public R4ImmunizationController(
      @Autowired R4Bundler bundler,
      @Autowired ImmunizationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private static Specification<ImmunizationEntity> anyCodeSpec(
      @SuppressWarnings("unused") String unused) {
    return ImmunizationRepository.AnyCodeSpecification.builder().build();
  }

  private Immunization.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Immunization> reports, int totalRecords) {
    log.info("Search {} found {} results", parameters, totalRecords);
    return bundler.bundle(
        PageLinks.LinkConfig.builder()
            .path("Immunization")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build(),
        reports,
        Immunization.Entry::new,
        Immunization.Bundle::new);
  }

  private Immunization.Bundle bundle(
      MultiValueMap<String, String> parameters, int count, Page<ImmunizationEntity> entities) {
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entities.getTotalElements());
    }
    return bundle(
        parameters,
        replaceReferences(
                entities
                    .get()
                    .map(ImmunizationEntity::asDatamartImmunization)
                    .collect(Collectors.toList()))
            .stream()
            .map(this::transform)
            .collect(Collectors.toList()),
        (int) entities.getTotalElements());
  }

  private ImmunizationEntity findEntityById(String publicId) {
    Optional<ImmunizationEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Immunization read(@PathVariable("publicId") String publicId) {
    DatamartImmunization dm = findEntityById(publicId).asDatamartImmunization();
    replaceReferences(List.of(dm));
    return R4ImmunizationTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Get raw DatamartImmunization by id. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    ImmunizationEntity entity = findEntityById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  Collection<DatamartImmunization> replaceReferences(Collection<DatamartImmunization> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource ->
            Stream.of(
                resource.patient(),
                resource.performer().orElse(null),
                resource.requester().orElse(null),
                resource.location().orElse(null)));
    return resources;
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Immunization.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(publicId, page, count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Immunization.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build();
    Immunization resource = read(identifier);
    int totalRecords = resource == null ? 0 : 1;
    if (resource == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, asList(resource), totalRecords);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public Immunization.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String icn = witnessProtection.toCdwId(patient);
    log.info("Looking for {} ({})", patient, icn);
    return bundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        count,
        repository.findByIcn(
            icn,
            PageRequest.of(page - 1, count == 0 ? 1 : count, ImmunizationEntity.naturalOrder())));
  }

  /** Search by patient and status. */
  @GetMapping(params = {"patient", "status"})
  public Immunization.Bundle searchByPatientAndStatus(
      @RequestParam("patient") String patient,
      @RequestParam("status") String status,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    TokenParameter statusToken = TokenParameter.parse(status);
    if ((statusToken.hasExplicitSystem()
            && !statusToken.hasAllowedSystem("http://hl7.org/fhir/event-status"))
        || statusToken.hasNoSystem()) {
      return Immunization.Bundle.builder().build();
    }
    Specification<ImmunizationEntity> searchSpec =
        statusToken
            .behavior()
            .onExplicitSystemAndAnyCode(s -> anyCodeSpec(s))
            .onExplicitSystemAndExplicitCode(
                (s, c) -> ImmunizationRepository.CodeSpecification.builder().code(c).build())
            .onAnySystemAndExplicitCode(
                c -> ImmunizationRepository.CodeSpecification.builder().code(c).build())
            .build()
            .execute();
    String icn = witnessProtection.toCdwId(patient);
    log.info("Looking for {} ({})", patient, icn);
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .add("status", status)
            .add("page", page)
            .add("_count", count)
            .build();
    searchSpec.and(ImmunizationRepository.PatientSpecification.builder().patient(patient).build());
    Page<ImmunizationEntity> entitiesPage =
        repository.findAll(
            searchSpec,
            PageRequest.of(page - 1, count == 0 ? 1 : count, ImmunizationEntity.naturalOrder()));
    return bundle(parameters, count, entitiesPage);
  }

  Immunization transform(DatamartImmunization dm) {
    return R4ImmunizationTransformer.builder().datamart(dm).build().toFhir();
  }
}
