package gov.va.api.health.dataquery.service.controller.practitionerrole;

import static java.util.Collections.emptyList;

import com.google.common.collect.Iterables;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.Stu3Bundler;
import gov.va.api.health.dataquery.service.controller.Stu3Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.practitioner.DatamartPractitioner;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerEntity;
import gov.va.api.health.dataquery.service.controller.practitioner.PractitionerRepository;
import gov.va.api.health.stu3.api.resources.OperationOutcome;
import gov.va.api.health.stu3.api.resources.PractitionerRole;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import lombok.AllArgsConstructor;
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
 * Request Mappings for Practitioner Role Profile, see
 * https://www.fhir.org/guides/argonaut/pd/StructureDefinition-argo-practitionerrole.html for
 * implementation details.
 */
@Validated
@RestController
@RequestMapping(
    value = "/stu3/PractitionerRole",
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
@SuppressWarnings("WeakerAccess")
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class Stu3PractitionerRoleController {
  private Stu3Bundler bundler;

  private PractitionerRepository repository;

  private WitnessProtection witnessProtection;

  private static PageRequest page(int page, int count) {
    return PageRequest.of(page - 1, count == 0 ? 1 : count, PractitionerEntity.naturalOrder());
  }

  private PractitionerRole.Bundle bundle(
      MultiValueMap<String, String> parameters,
      List<PractitionerRole> resources,
      int totalRecords) {
    return bundler.bundle(
        PageLinks.LinkConfig.builder()
            .path("PractitionerRole")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build(),
        resources,
        PractitionerRole.Entry::new,
        PractitionerRole.Bundle::new);
  }

  private PractitionerEntity entityById(String publicId) {
    Optional<PractitionerEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public PractitionerRole read(@PathVariable("publicId") String publicId) {
    PractitionerEntity entity = entityById(publicId);
    return Iterables.getOnlyElement(transform(Stream.of(entity)));
  }

  /** Read raw. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    IncludesIcnMajig.addHeaderForNoPatients(response);
    return entityById(publicId).payload();
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public PractitionerRole.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    PractitionerRole resource = read(publicId);
    return bundle(
        Parameters.builder()
            .add("identifier", publicId)
            .add("page", page)
            .add("_count", count)
            .build(),
        resource == null || count == 0 ? emptyList() : List.of(resource),
        resource == null ? 0 : 1);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public PractitionerRole.Bundle searchByIdentifier(
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(publicId, page, count);
  }

  /** Search by name. */
  @GetMapping(params = {"practitioner.family", "given"})
  public PractitionerRole.Bundle searchByName(
      @RequestParam("practitioner.family") String family,
      @RequestParam("given") String given,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("practitioner.family", family)
            .add("given", given)
            .add("page", page)
            .add("_count", count)
            .build();

    Page<PractitionerEntity> entitiesPage =
        repository.findByFamilyNameAndGivenName(family, given, page(page, count));
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    return bundle(parameters, transform(entitiesPage.get()), (int) entitiesPage.getTotalElements());
  }

  /** Search by NPI. */
  @GetMapping(params = {"practitioner.identifier"})
  public PractitionerRole.Bundle searchByNpi(
      @RequestParam("practitioner.identifier") String systemAndCode,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("practitioner.identifier", systemAndCode)
            .add("page", page)
            .add("_count", count)
            .build();
    int delimiterIndex = systemAndCode.lastIndexOf("|");
    if (delimiterIndex <= -1) {
      throw new ResourceExceptions.BadSearchParameter("Cannot parse NPI for " + systemAndCode);
    }

    String system = systemAndCode.substring(0, delimiterIndex);
    if (!system.equalsIgnoreCase("http://hl7.org/fhir/sid/us-npi")) {
      throw new ResourceExceptions.BadSearchParameter(
          String.format("System %s is not supported", system));
    }

    String npi = systemAndCode.substring(delimiterIndex + 1);
    Page<PractitionerEntity> entitiesPage = repository.findByNpi(npi, page(page, count));
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    return bundle(parameters, transform(entitiesPage.get()), (int) entitiesPage.getTotalElements());
  }

  /** Search by specialty. */
  @SuppressWarnings("unused")
  @GetMapping(params = {"specialty"})
  public PractitionerRole.Bundle searchBySpecialty(
      @RequestParam("specialty") String specialty,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    throw new ResourceExceptions.NotImplemented("not-implemented");
  }

  private List<PractitionerRole> transform(Stream<PractitionerEntity> entities) {
    List<DatamartPractitioner> datamarts =
        entities.map(PractitionerEntity::asDatamartPractitioner).collect(Collectors.toList());
    witnessProtection.registerAndUpdateReferences(
        datamarts,
        resource ->
            Stream.concat(
                resource.practitionerRole().stream()
                    .map(role -> role.managingOrganization().orElse(null)),
                resource.practitionerRole().stream().flatMap(role -> role.location().stream())));
    return datamarts.stream()
        .map(dm -> Stu3PractitionerRoleTransformer.builder().datamart(dm).build().toFhir())
        .collect(Collectors.toList());
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
      value = "/$validate",
      consumes = {"application/json", "application/json+fhir", "application/fhir+json"})
  public OperationOutcome validate(@RequestBody PractitionerRole.Bundle bundle) {
    return Stu3Validator.create().validate(bundle);
  }
}
