package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import gov.va.api.health.uscorer4.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
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
 * Request Mappings for Allergy Intolerance Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-allergyintolerance.html for
 * implementation details.
 */
@Validated
@RestController
@RequestMapping(
    value = "/r4/AllergyIntolerance",
    produces = {"application/json", "application/fhir+json"})
public class R4AllergyIntoleranceController {
  private R4Bundler bundler;

  private WitnessProtection witnessProtection;

  private AllergyIntoleranceRepository repository;

  /** Autowired constructor. */
  public R4AllergyIntoleranceController(
      @Autowired R4Bundler bundler,
      @Autowired AllergyIntoleranceRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private AllergyIntolerance.Bundle bundle(
      MultiValueMap<String, String> parameters,
      List<AllergyIntolerance> records,
      int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("AllergyIntolerance")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        linkConfig, records, AllergyIntolerance.Entry::new, AllergyIntolerance.Bundle::new);
  }

  AllergyIntoleranceEntity entityById(String publicId) {
    Optional<AllergyIntoleranceEntity> entity =
        repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by ID. */
  @GetMapping(value = {"/{publicId}"})
  public AllergyIntolerance read(@PathVariable("publicId") String publicId) {
    DatamartAllergyIntolerance dm = entityById(publicId).asDatamartAllergyIntolerance();
    replaceReferences(List.of(dm));
    return R4AllergyIntoleranceTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Return the raw datamart document for the given identifier. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    AllergyIntoleranceEntity entity = entityById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  private void replaceReferences(Collection<DatamartAllergyIntolerance> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource ->
            Stream.concat(
                Stream.of(resource.recorder().orElse(null), resource.patient()),
                resource.notes().stream().map(n -> n.practitioner().orElse(null))));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public AllergyIntolerance.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(id, page, count);
  }

  /** Search by identifier. */
  @GetMapping(params = {"identifier"})
  public AllergyIntolerance.Bundle searchByIdentifier(
      @RequestParam("identifier") String identifier,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("identifier", identifier)
            .add("page", page)
            .add("_count", count)
            .build();
    AllergyIntolerance resource = read(identifier);
    int totalRecords = resource == null ? 0 : 1;
    if (resource == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, asList(resource), totalRecords);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public AllergyIntolerance.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build();
    String cdwIcn = witnessProtection.toCdwId(patient);
    Page<AllergyIntoleranceEntity> entitiesPage =
        repository.findByIcn(
            cdwIcn,
            PageRequest.of(
                page - 1, count == 0 ? 1 : count, AllergyIntoleranceEntity.naturalOrder()));
    if (count <= 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartAllergyIntolerance> datamarts =
        entitiesPage
            .stream()
            .map(e -> e.asDatamartAllergyIntolerance())
            .collect(Collectors.toList());
    replaceReferences(datamarts);
    List<AllergyIntolerance> fhir =
        datamarts
            .stream()
            .map(dm -> R4AllergyIntoleranceTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, fhir, (int) entitiesPage.getTotalElements());
  }
}
