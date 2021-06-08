package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Specifications.strings;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;
import static gov.va.api.lighthouse.vulcan.VulcanConfiguration.PagingConfiguration.noSortableParameters;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.lighthouse.vulcan.Specifications;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import gov.va.api.lighthouse.vulcan.mappings.TokenParameter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
@AllArgsConstructor(onConstructor_ = @Autowired)
public class R4ObservationController {
  private static final String OBSERVATION_CATEGORY_SYSTEM =
      "http://terminology.hl7.org/CodeSystem/observation-category";

  private static final String OBSERVATION_CODE_SYSTEM = "http://loinc.org";

  private final LinkProperties linkProperties;

  private final ObservationRepository repository;

  private WitnessProtection witnessProtection;

  private VulcanConfiguration<ObservationEntity> configuration() {
    return VulcanConfiguration.<ObservationEntity>forEntity(ObservationEntity.class)
        .paging(
            linkProperties.pagingConfiguration(
                "Observation", ObservationEntity.naturalOrder(), noSortableParameters()))
        .mappings(
            Mappings.forEntity(ObservationEntity.class)
                .tokens(
                    "category", this::tokenCategoryIsSupported, this::tokenCategorySpecification)
                .tokens("code", this::tokenCodeIsSupported, this::tokenCodeSpecification)
                .dateAsInstant("date", "dateUtc")
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .value("patient", "icn")
                .get())
        .rules(
            List.of(
                parametersNeverSpecifiedTogether("_id", "identifier", "patient"),
                ifParameter("date").thenAlsoAtLeastOneParameterOf("category", "code"),
                ifParameter("category").thenAlsoAtLeastOneParameterOf("patient"),
                ifParameter("code").thenAlsoAtLeastOneParameterOf("patient")))
        .defaultQuery(returnNothing())
        .build();
  }

  /** Read R4 Observation By Id. */
  @GetMapping(value = "/{publicId}")
  public Observation read(@PathVariable("publicId") String publicId) {
    return vulcanizedReader().read(publicId);
  }

  /** Return the raw datamart document for the given Observation Id. */
  @GetMapping(
      value = "/{publicId}",
      headers = {"raw=true"})
  public String readRaw(@PathVariable("publicId") String publicId, HttpServletResponse response) {
    return vulcanizedReader().readRaw(publicId, response);
  }

  /** R4 Observation Search Support. */
  @GetMapping
  public Observation.Bundle search(HttpServletRequest request) {
    return Vulcan.forRepo(repository)
        .config(configuration())
        .build()
        .search(request)
        .map(toBundle());
  }

  VulcanizedBundler<
          ObservationEntity,
          DatamartObservation,
          Observation,
          Observation.Entry,
          Observation.Bundle>
      toBundle() {
    return VulcanizedBundler.forTransformation(transformation())
        .bundling(
            Bundling.newBundle(Observation.Bundle::new)
                .newEntry(Observation.Entry::new)
                .linkProperties(linkProperties)
                .build())
        .build();
  }

  /**
   * Supported Categories:
   *
   * <p>laboratory
   *
   * <p>vital-signs
   *
   * <p>http://terminology.hl7.org/CodeSystem/observation-category|
   *
   * <p>http://terminology.hl7.org/CodeSystem/observation-category|laboratory
   *
   * <p>http://terminology.hl7.org/CodeSystem/observation-category|vital-signs
   */
  boolean tokenCategoryIsSupported(TokenParameter token) {
    boolean codeIsSupported = token.hasSupportedCode("laboratory", "vital-signs");
    return (token.hasSupportedSystem(OBSERVATION_CATEGORY_SYSTEM)
            && (token.hasAnyCode() || codeIsSupported))
        || (token.hasAnySystem() && codeIsSupported);
  }

  private Specification<ObservationEntity> tokenCategorySpecification(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndAnyCode(
            s ->
                Specifications.<ObservationEntity>selectInList(
                    "category", strings("laboratory", "vital-signs")))
        .onAnySystemAndExplicitCode(c -> Specifications.<ObservationEntity>select("category", c))
        .onExplicitSystemAndExplicitCode(
            (s, c) -> Specifications.<ObservationEntity>select("category", c))
        .build()
        .execute();
  }

  boolean tokenCodeIsSupported(TokenParameter token) {
    return token.hasSupportedSystem(OBSERVATION_CODE_SYSTEM) || token.hasAnySystem();
  }

  Specification<ObservationEntity> tokenCodeSpecification(TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndAnyCode(
            s -> {
              if (OBSERVATION_CODE_SYSTEM.equals(s)) {
                return (Specification<ObservationEntity>)
                    ObservationRepository.CodeSpecification.of(
                        Set.of(ObservationRepository.ANY_CODE_VALUE));
              }
              throw new IllegalStateException(
                  "Unsupported Code System: " + s + " Cannot build ExplicitSystemSpecification.");
            })
        .onExplicitSystemAndExplicitCode(
            (s, c) -> Specifications.<ObservationEntity>select("code", c))
        .onAnySystemAndExplicitCode(c -> Specifications.<ObservationEntity>select("code", c))
        .build()
        .execute();
  }

  VulcanizedTransformation<ObservationEntity, DatamartObservation, Observation> transformation() {
    return VulcanizedTransformation.toDatamart(ObservationEntity::asDatamartObservation)
        .toResource(dm -> R4ObservationTransformer.builder().datamart(dm).build().toFhir())
        .witnessProtection(witnessProtection)
        .replaceReferences(
            resource ->
                Stream.concat(
                    Stream.of(resource.subject().orElse(null), resource.encounter().orElse(null)),
                    resource.performer().stream()))
        .build();
  }

  VulcanizedReader<ObservationEntity, DatamartObservation, Observation, String> vulcanizedReader() {
    return VulcanizedReader
        .<ObservationEntity, DatamartObservation, Observation, String>forTransformation(
            transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPrimaryKey(Function.identity())
        .toPayload(ObservationEntity::payload)
        .build();
  }
}
