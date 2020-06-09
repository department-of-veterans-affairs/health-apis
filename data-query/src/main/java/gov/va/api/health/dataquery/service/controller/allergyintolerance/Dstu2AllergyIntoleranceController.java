package gov.va.api.health.dataquery.service.controller.allergyintolerance;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.AllergyIntolerance;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions.NotFound;
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
 * http://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-allergyintolerance.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
    value = {"/dstu2/AllergyIntolerance"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class Dstu2AllergyIntoleranceController {

  private Dstu2Bundler bundler;

  private WitnessProtection witnessProtection;

  private AllergyIntoleranceRepository repository;

  /** Autowired constructor. */
  public Dstu2AllergyIntoleranceController(
      @Autowired Dstu2Bundler bundler,
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
        Dstu2Bundler.BundleContext.of(
            linkConfig, records, AllergyIntolerance.Entry::new, AllergyIntolerance.Bundle::new));
  }

  AllergyIntoleranceEntity findById(String publicId) {
    Optional<AllergyIntoleranceEntity> entity =
        repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public AllergyIntolerance read(@PathVariable("publicId") String publicId) {
    DatamartAllergyIntolerance dm = findById(publicId).asDatamartAllergyIntolerance();
    replaceReferences(List.of(dm));
    return Dstu2AllergyIntoleranceTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    AllergyIntoleranceEntity entity = findById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  void replaceReferences(Collection<DatamartAllergyIntolerance> resources) {
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
    return searchByPatient(parameters);
  }

  AllergyIntolerance.Bundle searchByPatient(MultiValueMap<String, String> publicParameters) {
    String publicIcn = publicParameters.getFirst("patient");
    String cdwIcn = witnessProtection.toCdwId(publicIcn);
    int page = Parameters.pageOf(publicParameters);
    int count = Parameters.countOf(publicParameters);
    Page<AllergyIntoleranceEntity> entitiesPage =
        repository.findByIcn(
            cdwIcn,
            PageRequest.of(
                page - 1, count == 0 ? 1 : count, AllergyIntoleranceEntity.naturalOrder()));
    if (Parameters.countOf(publicParameters) <= 0) {
      return bundle(publicParameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartAllergyIntolerance> datamarts =
        entitiesPage.stream()
            .map(e -> e.asDatamartAllergyIntolerance())
            .collect(Collectors.toList());
    replaceReferences(datamarts);
    List<AllergyIntolerance> fhir =
        datamarts.stream()
            .map(dm -> Dstu2AllergyIntoleranceTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(publicParameters, fhir, (int) entitiesPage.getTotalElements());
  }
}
