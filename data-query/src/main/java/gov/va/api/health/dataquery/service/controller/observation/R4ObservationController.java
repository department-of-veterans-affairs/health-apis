package gov.va.api.health.dataquery.service.controller.observation;

import static java.util.Collections.emptyList;

import com.google.common.base.Splitter;
import gov.va.api.health.dataquery.service.controller.CountParameter;
import gov.va.api.health.dataquery.service.controller.DateTimeParameter;
import gov.va.api.health.dataquery.service.controller.IncludesIcnMajig;
import gov.va.api.health.dataquery.service.controller.PageLinks;
import gov.va.api.health.dataquery.service.controller.Parameters;
import gov.va.api.health.dataquery.service.controller.R4Bundler;
import gov.va.api.health.dataquery.service.controller.ResourceExceptions;
import gov.va.api.health.dataquery.service.controller.TokenParameter;
import gov.va.api.health.dataquery.service.controller.TokenParameter.Mode;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.r4.api.bundle.AbstractEntry;
import gov.va.api.health.uscorer4.api.resources.Observation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Request Mappings for Observation Profile, see
 * https://build.fhir.org/ig/HL7/US-Core-R4/StructureDefinition-us-core-observation-lab.html for
 * implementation details.
 */
@Validated
@RestController
@RequestMapping(
    value = {"/r4/Observation"},
    produces = {"application/json", "application/fhir+json"})
public class R4ObservationController {
  private static final String OBSERVATION_CATEGORY_SYSTEM =
      "http://terminology.hl7.org/CodeSystem/observation-category";

  private static final String OBSERVATION_CODE_SYSTEM = "http://loinc.org";

  private final R4Bundler bundler;

  private final ObservationRepository repository;

  private final WitnessProtection witnessProtection;

  /** Constructor. */
  public R4ObservationController(
      @Autowired R4Bundler bundler,
      @Autowired ObservationRepository repository,
      @Autowired WitnessProtection witnessProtection) {
    this.bundler = bundler;
    this.repository = repository;
    this.witnessProtection = witnessProtection;
  }

  @SuppressWarnings("RedundantIfStatement")
  private static boolean isSupportedCategoryValue(TokenParameter t) {
    // laboratory
    // vital-signs
    if (t.mode() == Mode.ANY_SYSTEM_EXPLICIT_CODE
        && t.isCodeExplicitlySetAndOneOf("laboratory", "vital-signs")) {
      return true;
    }
    // http://terminology.hl7.org/CodeSystem/observation-category|laboratory
    // http://terminology.hl7.org/CodeSystem/observation-category|vital-signs
    if (t.mode() == Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE
        && t.isSystemExplicitlySetAndOneOf(OBSERVATION_CATEGORY_SYSTEM)
        && t.isCodeExplicitlySetAndOneOf("laboratory", "vital-signs")) {
      return true;
    }
    // http://terminology.hl7.org/CodeSystem/observation-category|
    if (t.mode() == Mode.EXPLICIT_SYSTEM_ANY_CODE
        && t.isSystemExplicitlySetAndOneOf(OBSERVATION_CATEGORY_SYSTEM)) {
      return true;
    }
    // bar
    // http://foo.com|bar
    // http://foo.com|laboratory
    return false;
  }

  @SuppressWarnings("RedundantIfStatement")
  private static boolean isSupportedCodeValue(TokenParameter t) {
    // 12345
    if (t.mode() == Mode.ANY_SYSTEM_EXPLICIT_CODE) {
      return true;
    }
    // http://loinc.org|12345
    if (t.mode() == Mode.EXPLICIT_SYSTEM_EXPLICIT_CODE
        && t.isSystemExplicitlySetAndOneOf(OBSERVATION_CODE_SYSTEM)) {
      return true;
    }
    // http://loinc.org|
    if (t.mode() == Mode.EXPLICIT_SYSTEM_ANY_CODE
        && t.isSystemExplicitlySetAndOneOf(OBSERVATION_CODE_SYSTEM)) {
      return true;
    }
    // http://foo.com|12345
    // http://foo.com|
    return false;
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
    return bundler.bundle(linkConfig, records, Observation.Entry::new, Observation.Bundle::new);
  }

  Observation.Bundle bundle(
      MultiValueMap<String, String> parameters, Page<ObservationEntity> entitiesPage) {
    if (Parameters.countOf(parameters) <= 0) {
      return bundle(parameters, emptyList(), (int) entitiesPage.getTotalElements());
    }
    List<DatamartObservation> datamartObservations =
        entitiesPage.stream()
            .map(ObservationEntity::asDatamartObservation)
            .collect(Collectors.toList());
    replaceReferences(datamartObservations);
    List<Observation> fhir =
        datamartObservations.stream()
            .map(dm -> R4ObservationTransformer.builder().datamart(dm).build().toFhir())
            .collect(Collectors.toList());
    return bundle(parameters, fhir, (int) entitiesPage.getTotalElements());
  }

  /**
   * A category csv can contain any combination of code, system|code, system|, |code, or system
   * tokens. Therefore, we need to iterate over the list one by one.
   */
  private Specification<ObservationEntity> categoryClauseFor(
      @NotEmpty List<TokenParameter> categoryTokens) {
    // Spring doesn't want to OR the same spec together more than once.
    // This collects all codes based on the same criteria TokenParameter uses for
    // determining behavior.
    Set<String> categoriesForQuery =
        categoryTokens.stream()
            .map(this::categoryFor)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    return ObservationRepository.CategorySpecification.of(categoriesForQuery);
  }

  /** Determine the category based on the a tokens value. */
  private List<String> categoryFor(TokenParameter categoryToken) {
    return categoryToken
        .behavior()
        .onExplicitSystemAndExplicitCode((s, c) -> List.of(c))
        .onAnySystemAndExplicitCode(List::of)
        .onNoSystemAndExplicitCode(List::of)
        .onExplicitSystemAndAnyCode(
            s -> {
              if (OBSERVATION_CATEGORY_SYSTEM.equals(s)) {
                return List.of("laboratory", "vital-signs");
              }
              throw new IllegalStateException(
                  "Unsupported Category System: "
                      + s
                      + " Cannot build ExplicitSystemSpecification.");
            })
        .build()
        .execute();
  }

  private Specification<ObservationEntity> codeClauseFor(
      @NotEmpty List<TokenParameter> codeTokens) {
    // Spring doesn't want to OR the same spec together more than once.
    // This collects all codes based on the same criteria TokenParameter uses for
    // determining behavior.
    Set<String> codesForQuery =
        codeTokens.stream().flatMap(c -> codeFor(c).stream()).collect(Collectors.toSet());
    return ObservationRepository.CodeSpecification.of(codesForQuery);
  }

  /** Determine the code based on the a tokens value. */
  private List<String> codeFor(TokenParameter codeToken) {
    return codeToken
        .behavior()
        .onExplicitSystemAndExplicitCode((s, c) -> List.of(c))
        .onAnySystemAndExplicitCode(List::of)
        .onNoSystemAndExplicitCode(List::of)
        .onExplicitSystemAndAnyCode(
            s -> {
              if (OBSERVATION_CODE_SYSTEM.equals(s)) {
                return List.of(s);
              }
              throw new IllegalStateException(
                  "Unsupported Code System: " + s + " Cannot build ExplicitSystemSpecification.");
            })
        .build()
        .execute();
  }

  ObservationEntity findById(String publicId) {
    String cdwId = witnessProtection.toCdwId(publicId);
    Optional<ObservationEntity> maybeEntity = repository.findById(cdwId);
    return maybeEntity.orElseThrow(() -> new ResourceExceptions.NotFound(publicId));
  }

  Pageable page(int page, int count) {
    return PageRequest.of(page - 1, Math.max(count, 1), ObservationEntity.naturalOrder());
  }

  /** Read R4 Observation By Id. */
  @GetMapping(value = "/{publicId}")
  public Observation read(@PathVariable("publicId") String publicId) {
    DatamartObservation dm = findById(publicId).asDatamartObservation();
    replaceReferences(List.of(dm));
    return R4ObservationTransformer.builder().datamart(dm).build().toFhir();
  }

  /** Return the raw datamart document for the given Observation Id. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    ObservationEntity entity = findById(publicId);
    IncludesIcnMajig.addHeader(response, entity.icn());
    return entity.payload();
  }

  void replaceReferences(Collection<DatamartObservation> resources) {
    // Omits Observation References that are unsupported
    witnessProtection.registerAndUpdateReferences(
        resources,
        resource ->
            Stream.concat(
                Stream.of(resource.subject().orElse(null)), resource.performer().stream()));
  }

  /** Search R4 Observation by _id. */
  @GetMapping(params = {"_id"})
  public Observation.Bundle searchById(
      @RequestParam("_id") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    return searchByIdentifier(id, page, count);
  }

  /** Search R4 Observation by identifier. */
  @GetMapping(params = {"identifier"})
  public Observation.Bundle searchByIdentifier(
      @RequestParam("identifier") String id,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("identifier", id).add("page", page).add("_count", count).build();
    Observation resource = read(id);
    int totalRecords = resource == null ? 0 : 1;
    if (resource == null || page != 1 || count <= 0) {
      return bundle(parameters, emptyList(), totalRecords);
    }
    return bundle(parameters, List.of(resource), totalRecords);
  }

  /** Search R4 Observation by Patient. */
  @GetMapping(params = {"patient"})
  public Observation.Bundle searchByPatient(
      @RequestHeader(name = "query-hack", required = false, defaultValue = "true")
          boolean queryHack,
      @RequestParam("patient") String patient,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder().add("patient", patient).add("page", page).add("_count", count).build();
    String cdwPatient = witnessProtection.toCdwId(patient);
    if (queryHack) {
      Observation.Bundle bundleWithWrongPageLinks =
          searchByPatientAndCategory(true, patient, "laboratory,vital-signs", null, page, count);
      return bundle(
          parameters,
          bundleWithWrongPageLinks.entry().stream()
              .map(AbstractEntry::resource)
              .collect(Collectors.toList()),
          bundleWithWrongPageLinks.total());
    }
    Page<ObservationEntity> entitiesPage = repository.findByIcn(cdwPatient, page(page, count));
    return bundle(parameters, entitiesPage);
  }

  /** Search R4 Observation by patient and category (and date if exists). */
  @GetMapping(params = {"patient", "category"})
  public Observation.Bundle searchByPatientAndCategory(
      @RequestHeader(name = "query-hack", required = false, defaultValue = "true")
          boolean queryHack,
      @RequestParam("patient") String patient,
      @RequestParam("category") String categoryCsv,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .add("category", categoryCsv)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build();
    String cdwPatient = witnessProtection.toCdwId(patient);
    List<TokenParameter> tokens =
        Splitter.on(",").trimResults().splitToList(categoryCsv).stream()
            .map(TokenParameter::parse)
            .filter(R4ObservationController::isSupportedCategoryValue)
            .collect(Collectors.toList());
    if (tokens.isEmpty()) {
      return bundle(parameters, emptyList(), 0);
    }
    Specification<ObservationEntity> spec =
        ObservationRepository.PatientAndDateSpecification.builder()
            .patient(cdwPatient)
            .dates(date)
            .build()
            .and(categoryClauseFor(tokens));
    if (queryHack) {
      List<ObservationEntity> all = repository.findAll(spec);
      int firstIndex = Math.min((page - 1) * count, all.size());
      int lastIndex = Math.min(firstIndex + count, all.size());
      List<DatamartObservation> pageOfEntities =
          firstIndex >= all.size()
              ? emptyList()
              : all.subList(firstIndex, lastIndex).stream()
                  .map(ObservationEntity::asDatamartObservation)
                  .collect(Collectors.toList());
      replaceReferences(pageOfEntities);
      List<Observation> pageOfResults =
          pageOfEntities.stream()
              .map(dm -> R4ObservationTransformer.builder().datamart(dm).build().toFhir())
              .collect(Collectors.toList());
      return bundle(parameters, pageOfResults, all.size());
    }
    Page<ObservationEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(parameters, entitiesPage);
  }

  /** Search R4 Observation by patient and code. */
  @GetMapping(params = {"patient", "code"})
  public Observation.Bundle searchByPatientAndCode(
      @RequestParam("patient") String patient,
      @RequestParam("code") String codeCsv,
      @RequestParam(value = "date", required = false) @Valid @DateTimeParameter @Size(max = 2)
          String[] date,
      @RequestParam(value = "page", defaultValue = "1") @Min(1) int page,
      @CountParameter @Min(0) int count) {
    MultiValueMap<String, String> parameters =
        Parameters.builder()
            .add("patient", patient)
            .add("code", codeCsv)
            .addAll("date", date)
            .add("page", page)
            .add("_count", count)
            .build();
    String cdwPatient = witnessProtection.toCdwId(patient);
    List<TokenParameter> tokens =
        Splitter.on(",").trimResults().splitToList(codeCsv).stream()
            .map(TokenParameter::parse)
            .filter(R4ObservationController::isSupportedCodeValue)
            .collect(Collectors.toList());
    if (tokens.isEmpty()) {
      return bundle(parameters, emptyList(), 0);
    }
    Specification<ObservationEntity> spec =
        ObservationRepository.PatientAndDateSpecification.builder()
            .patient(cdwPatient)
            .dates(date)
            .build()
            .and(codeClauseFor(tokens));
    Page<ObservationEntity> entitiesPage = repository.findAll(spec, page(page, count));
    return bundle(parameters, entitiesPage);
  }
}
