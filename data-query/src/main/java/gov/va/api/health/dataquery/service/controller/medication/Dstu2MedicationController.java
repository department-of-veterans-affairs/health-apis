package gov.va.api.health.dataquery.service.controller.medication;

import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.Medication;
import gov.va.api.health.argonaut.api.resources.Medication.Bundle;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Medication Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medication.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
    value = {"/dstu2/Medication"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class Dstu2MedicationController {

  private Dstu2Bundler bundler;

  private MedicationRepository repository;

  private WitnessProtection witnessProtection;

  /** Spring constructor. */
  @SuppressWarnings("ParameterHidesMemberVariable")
  public Dstu2MedicationController(
      @Autowired Dstu2Bundler bundler,
      @Autowired MedicationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Bundle bundle(
      MultiValueMap<String, String> parameters, List<Medication> reports, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Medication")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig, reports, Medication.Entry::new, Medication.Bundle::new));
  }

  MedicationEntity findById(String publicId) {
    Optional<MedicationEntity> entity = repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Medication read(@PathVariable("publicId") String publicId) {
    DatamartMedication medication = findById(publicId).asDatamartMedication();
    witnessProtection.registerAndUpdateReferences(List.of(medication), resource -> Stream.empty());
    return Dstu2MedicationTransformer.builder().datamart(medication).build().toFhir();
  }

  /** Read by id, raw data. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    IncludesIcnMajig.addHeaderForNoPatients(response);
    return findById(publicId).payload();
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    Medication resource = read(id);
    return bundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        resource == null || count == 0 ? emptyList() : List.of(resource),
        resource == null ? 0 : 1);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(id, page, count);
  }
}
