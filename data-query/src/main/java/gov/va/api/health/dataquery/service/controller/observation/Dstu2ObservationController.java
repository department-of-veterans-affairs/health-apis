package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import com.google.common.base.Splitter;
import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.Dstu2Bundler;
import gov.va.api.health.dataquery.service.controller.Dstu2Validator;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
 * Request Mappings for Observation Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-Observation.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"/dstu2/Observation"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class Dstu2ObservationController {

  private Dstu2Bundler bundler;

  private WitnessProtection witnessProtection;

  private ObservationRepository repository;

  /** Autowired constructor. */
  public Dstu2ObservationController(
      @Autowired Dstu2Bundler bundler,
      @Autowired ObservationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  Observation.Bundle bundle(
      MultiValueMap<String, String> parameters, List<Observation> records, int totalRecords) {
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Observation")
            .queryParams(parameters)
            .page(Parameters.pageOf(parameters))
            .recordsPerPage(Parameters.countOf(parameters))
            .totalRecords(totalRecords)
            .build();
    return bundler.bundle(
        Dstu2Bundler.BundleContext.of(
            linkConfig, records, Observation.Entry::new, Observation.Bundle::new));
  }

  Observation.Bundle bundle(
      MultiValueMap<String, String> parameters, Page<ObservationEntity> entitiesPage) {
    if (Parameters.countOf(parameters) <= 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartObservation> datamarts =
        entitiesPage.stream().map(e -> e.asDatamartObservation()).collect(Collectors.toList());
    replaceReferences(datamarts);
    List<Observation> fhir =
        datamarts
            .stream()
            .map(dm -> Dstu2ObservationTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, fhir, (int) entitiesPage.getTotalElements());
  }

  ObservationEntity findById(@PathVariable("publicId") String publicId) {
    String cdwId = witnessProtection.toCdwId(publicId);
    Optional<ObservationEntity> maybeEntity = repository.findById(cdwId);
    if (!maybeEntity.isPresent()) {
      throw new ResourceExceptions.NotFound(publicId);
    }
    return maybeEntity.get();
  }

  Pageable page(int page, int count) {
    return PageRequest.of(page - 1, Math.max(count, 1), ObservationEntity.naturalOrder());
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Observation read(@PathVariable("publicId") String publicId) {
    DatamartObservation dm = findById(publicId).asDatamartObservation();
    replaceReferences(List.of(dm));
    return Dstu2ObservationTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
    value = "/{publicId}",
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    ObservationEntity entity = findById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  void replaceReferences(Collection<DatamartObservation> resources) {
    // Specimen is omitted because we do not support that resource.
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource ->
            Stream.concat(
                Stream.of(resource.subject().orElse(null), resource.encounter().orElse(null)),
                resource.performer().stream()));
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Observation.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build();
    Observation resource = read(id);
    int totalRecords = resource == null ? 0 : 1;
    if (resource == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, asList(resource), totalRecords);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Observation.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchById(id, page, count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public Observation.Bundle searchByPatient(
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String cdwPatient = witnessProtection.toCdwId(patient);
    Page<ObservationEntity> entitiesPage = repository.findByIcn(cdwPatient, page(page, count));
    return bundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        entitiesPage);
  }

  /** Search by patient and category and date if available. */
  @GetMapping(params = {"patient", "category"})
  public Observation.Bundle searchByPatientAndCategory(
      @RequestParam("patient") String patient,
      @RequestParam("category") String categoryCsv,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String cdwPatient = witnessProtection.toCdwId(patient);
    ObservationRepository.PatientAndCategoryAndDateSpecification spec =
        ObservationRepository.PatientAndCategoryAndDateSpecification.builder()
            .patient(cdwPatient)
            .categories(Splitter.on(",").trimResults().splitToList(categoryCsv))
            .dates(date)
            .build();
    Page<ObservationEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("category", categoryCsv)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build(),
        entitiesPage);
  }

  /** Search by patient and code. */
  @GetMapping(params = {"patient", "code"})
  public Observation.Bundle searchByPatientAndCode(
      @RequestParam("patient") String patient,
      @RequestParam("code") String codeCsv,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    String cdwPatient = witnessProtection.toCdwId(patient);
    ObservationRepository.PatientAndCodesSpecification spec =
        ObservationRepository.PatientAndCodesSpecification.builder()
            .patient(cdwPatient)
            .codes(Splitter.on(",").trimResults().splitToList(codeCsv))
            .build();
    Page<ObservationEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(
        Parameters.builder()
            .add("patient", patient)
            .add("code", codeCsv)
            .add("page", page)
            .add("_count", count)
            .build(),
        entitiesPage);
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Observation.Bundle bundle) {
    return Dstu2Validator.create().validate(bundle);
  }
}
