package gov.va.api.health.dataquery.service.controller.medicationstatement;

import static java.util.Collections.emptyList;

import gov.va.api.health.argonaut.api.resources.MedicationStatement;
import gov.va.api.health.argonaut.api.resources.MedicationStatement.Bundle;
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
 * Request Mappings for Medication Statement Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-medicationstatement.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Slf4j
@Validated
@RestController
@RequestMapping(
    value = {"/dstu2/MedicationStatement"},
    produces = {"application/json", "application/json+fhir", "application/fhir+json"})
public class Dstu2MedicationStatementController {

  private Dstu2Bundler bundler;

  private WitnessProtection witnessProtection;

  private MedicationStatementRepository repository;

  /** Spring constructor. */
  @SuppressWarnings("ParameterHidesMemberVariable")
  public Dstu2MedicationStatementController(
      @Autowired Dstu2Bundler bundler,
      @Autowired MedicationStatementRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Bundle bundle(
      MultiValueMap<String, String> parameters,
      List<MedicationStatement> reports,
      int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("MedicationStatement")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig, reports, MedicationStatement.Entry::new, MedicationStatement.Bundle::new));
  }

  private Bundle bundle(
      MultiValueMap<String, String> parameters,
      int count,
      Page<MedicationStatementEntity> entities) {
    log.info("Search {} found {} results", parameters, entities.getTotalElements());
    if (count == 0) {
      return bundle(parameters, emptyList(), (int) entities.getTotalElements());
    }
    return bundle(
        parameters,
        replaceReferences(
                entities
                    .get()
                    .map(MedicationStatementEntity::asDatamartMedicationStatement)
                    .collect(Collectors.toList()))
            .stream()
            .map(this::transform)
            .collect(Collectors.toList()),
        (int) entities.getTotalElements());
  }

  MedicationStatementEntity findById(String publicId) {
    Optional<MedicationStatementEntity> entity =
        repository.findById(witnessProtection.toCdwId(publicId));
    return entity.orElseThrow(() -> new NotFound(publicId));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public MedicationStatement read(@PathVariable("publicId") String publicId) {
    DatamartMedicationStatement medicationStatement =
        findById(publicId).asDatamartMedicationStatement();
    replaceReferences(List.of(medicationStatement));
    return transform(medicationStatement);
  }

  /** Read by id, raw data. */
  @GetMapping(
      value = {"/{publicId}"},
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    MedicationStatementEntity entity = findById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  Collection<DatamartMedicationStatement> replaceReferences(
      Collection<DatamartMedicationStatement> resources) {
    witnessProtection.registerAndUpdateReferences(
        resources, resource -> Stream.of(resource.patient(), resource.medication()));
    return resources;
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public MedicationStatement.Bundle searchById(
      @RequestParam("_id") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MedicationStatement resource = read(publicId);
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
  public MedicationStatement.Bundle searchByIdentifier(
      @RequestParam("identifier") String publicId,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(publicId, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public MedicationStatement.Bundle searchByPatient(
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
            PageRequest.of(
                page - 1, count == 0 ? 1 : count, MedicationStatementEntity.naturalOrder())));
  }

  MedicationStatement transform(DatamartMedicationStatement dm) {
    return Dstu2MedicationStatementTransformer.builder().datamart(dm).build().toFhir();
  }
}
