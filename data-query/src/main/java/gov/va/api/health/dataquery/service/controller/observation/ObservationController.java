package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.health.dataquery.service.controller.Transformers.firstPayloadItem;
import static gov.va.api.health.dataquery.service.controller.Transformers.hasPayload;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.base.Splitter;
import gov.va.api.health.argonaut.api.resources.Observation;
import gov.va.api.health.dataquery.service.controller.Bundler;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.Validator;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.mranderson.client.MrAndersonClient;
import gov.va.api.health.dataquery.service.mranderson.client.Query;
import gov.va.api.health.dstu2.api.resources.OperationOutcome;
import gov.va.dvp.cdw.xsd.model.CdwObservation104Root;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
 * Request Mappings for Observation Profile, see
 * https://www.fhir.org/guides/argonaut/r2/StructureDefinition-argo-Observation.html for
 * implementation details.
 */
@SuppressWarnings("WeakerAccess")
@Validated
@RestController
@RequestMapping(
  value = {"Observation", "/api/Observation"},
  produces = {"application/json", "application/json+fhir", "application/fhir+json"}
)
public class ObservationController {
  private final Datamart datamart = new Datamart();

  private boolean defaultToDatamart;

  private Transformer transformer;

  private MrAndersonClient mrAndersonClient;

  private Bundler bundler;

  private WitnessProtection witnessProtection;

  private ObservationRepository repository;

  /** Autowired constructor. */
  public ObservationController(
      @Value("${datamart.observation}") boolean defaultToDatamart,
      @Autowired Transformer transformer,
      @Autowired MrAndersonClient mrAndersonClient,
      @Autowired Bundler bundler,
      @Autowired ObservationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.defaultToDatamart = defaultToDatamart;
    this.transformer = transformer;
    this.mrAndersonClient = mrAndersonClient;
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  private Observation.Bundle mrAndersonBundle(
      MultiValueMap<String, String> parameters, int page, int count) {
    CdwObservation104Root root = mrAndersonSearch(parameters);
    PageLinks.LinkConfig linkConfig =
        PageLinks.LinkConfig.builder()
            .path("Observation")
            .queryParams(parameters)
            .page(page)
            .recordsPerPage(count)
            .totalRecords(root.getRecordCount().intValue())
            .build();
    return bundler.bundle(
        Bundler.BundleContext.of(
            linkConfig,
            root.getObservations() == null ? emptyList() : root.getObservations().getObservation(),
            transformer,
            Observation.Entry::new,
            Observation.Bundle::new));
  }

  private CdwObservation104Root mrAndersonSearch(MultiValueMap<String, String> params) {
    Query<CdwObservation104Root> query =
        Query.forType(CdwObservation104Root.class)
            .profile(Query.Profile.ARGONAUT)
            .resource("Observation")
            .version("1.04")
            .parameters(params)
            .build();
    return hasPayload(mrAndersonClient.search(query));
  }

  /** Read by id. */
  @GetMapping(value = {"/{publicId}"})
  public Observation read(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @PathVariable("publicId") String publicId) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.read(publicId);
    }
    return transformer.apply(
        firstPayloadItem(
            hasPayload(mrAndersonSearch(Parameters.forIdentity(publicId)).getObservations())
                .getObservation()));
  }

  /** Return the raw Datamart document for the given identifier. */
  @GetMapping(
    value = "/{publicId}",
    headers = {"raw=true"}
  )
  public String readRaw(@PathVariable("publicId") String publicId) {
    return datamart.readRaw(publicId);
  }

  /** Search by _id. */
  @GetMapping(params = {"_id"})
  public Observation.Bundle searchById(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchById(id, page, count);
    }
    return mrAndersonBundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by Identifier. */
  @GetMapping(params = {"identifier"})
  public Observation.Bundle searchByIdentifier(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return searchById(datamartHeader, id, page, count);
    }
    return mrAndersonBundle(
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by patient. */
  @GetMapping(params = {"patient"})
  public Observation.Bundle searchByPatient(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchByPatient(patient, page, count);
    }
    return mrAndersonBundle(
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build(),
        page,
        count);
  }

  /** Search by patient and category and date if available. */
  @GetMapping(params = {"patient", "category"})
  public Observation.Bundle searchByPatientAndCategory(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("patient") String patient,
      @RequestParam("category") String categoryCsv,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchByPatientAndCategoryAndDate(patient, categoryCsv, date, page, count);
    }
    return mrAndersonBundle(
        Parameters.builder()
            .add("patient", patient)
            .add("category", categoryCsv)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Search by patient and code. */
  @GetMapping(params = {"patient", "code"})
  public Observation.Bundle searchByPatientAndCode(
      @RequestHeader(value = "Datamart", defaultValue = "") String datamartHeader,
      @RequestParam("patient") String patient,
      @RequestParam("code") String codeCsv,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    if (datamart.isDatamartRequest(datamartHeader)) {
      return datamart.searchByPatientAndCode(patient, codeCsv, page, count);
    }
    return mrAndersonBundle(
        Parameters.builder()
            .add("patient", patient)
            .add("code", codeCsv)
            .add("page", page)
            .add("_count", count)
            .build(),
        page,
        count);
  }

  /** Hey, this is a validate endpoint. It validates. */
  @PostMapping(
    value = "/$validate",
    consumes = {"application/json", "application/json+fhir", "application/fhir+json"}
  )
  public OperationOutcome validate(@RequestBody Observation.Bundle bundle) {
    return Validator.create().validate(bundle);
  }

  public interface Transformer
      extends Function<CdwObservation104Root.CdwObservations.CdwObservation, Observation> {}

  /**
   * This class is being used to help organize the code such that all the datamart logic is
   * contained together. In the future when Mr. Anderson support is dropped, this class can be
   * eliminated.
   */
  private class Datamart {
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
          Bundler.BundleContext.of(
              linkConfig,
              records,
              Function.identity(),
              Observation.Entry::new,
              Observation.Bundle::new));
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
              .map(dm -> DatamartObservationTransformer.builder().datamart(dm).build().toFhir())
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

    boolean isDatamartRequest(String datamartHeader) {
      if (isBlank(datamartHeader)) {
        return defaultToDatamart;
      }
      return BooleanUtils.isTrue(BooleanUtils.toBooleanObject(datamartHeader));
    }

    Pageable page(int page, int count) {
      return PageRequest.of(page - 1, Math.max(count, 1), ObservationEntity.naturalOrder());
    }

    Observation read(String publicId) {
      DatamartObservation dm = findById(publicId).asDatamartObservation();
      replaceReferences(List.of(dm));
      return DatamartObservationTransformer.builder().datamart(dm).build().toFhir();
    }

    String readRaw(@PathVariable("publicId") String publicId) {
      return findById(publicId).payload();
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

    Observation.Bundle searchById(String id, int page, int count) {
      MultiValueMap<String, String> parameters =
          Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build();
      Observation resource = read(id);
      int totalRecords = resource == null ? 0 : 1;
      if (resource == null || page != 1 || count <= 0) {
        return bundle(parameters, emptyList(), totalRecords);
      }
      return bundle(parameters, asList(resource), totalRecords);
    }

    Observation.Bundle searchByPatient(String publicPatient, int page, int count) {
      String cdwPatient = witnessProtection.toCdwId(publicPatient);
      Page<ObservationEntity> entitiesPage = repository.findByIcn(cdwPatient, page(page, count));

      return bundle(
          Parameters.builder()
              .add("patient", publicPatient)
              .add("page", page)
              .add("_count", count)
              .build(),
          entitiesPage);
    }

    Observation.Bundle searchByPatientAndCategoryAndDate(
        String publicPatient, String categoryCsv, String[] date, int page, int count) {
      String cdwPatient = witnessProtection.toCdwId(publicPatient);

      ObservationRepository.PatientAndCategoryAndDateSpecification spec =
          ObservationRepository.PatientAndCategoryAndDateSpecification.builder()
              .patient(cdwPatient)
              .categories(Splitter.on(",").trimResults().splitToList(categoryCsv))
              .dates(date)
              .build();
      Page<ObservationEntity> entitiesPage = repository.findAll(spec, page(page, count));

      return bundle(
          Parameters.builder()
              .add("patient", publicPatient)
              .add("category", categoryCsv)
              .addAll("date", date)
              .add("page", page)
              .add("_count", count)
              .build(),
          entitiesPage);
    }

    Observation.Bundle searchByPatientAndCode(
        String publicPatient, String codeCsv, int page, int count) {
      String cdwPatient = witnessProtection.toCdwId(publicPatient);

      ObservationRepository.PatientAndCodesSpecification spec =
          ObservationRepository.PatientAndCodesSpecification.builder()
              .patient(cdwPatient)
              .codes(Splitter.on(",").trimResults().splitToList(codeCsv))
              .build();
      Page<ObservationEntity> entitiesPage = repository.findAll(spec, page(page, count));

      return bundle(
          Parameters.builder()
              .add("patient", publicPatient)
              .add("code", codeCsv)
              .add("page", page)
              .add("_count", count)
              .build(),
          entitiesPage);
    }
  }
}
