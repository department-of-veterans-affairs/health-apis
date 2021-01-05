package gov.va.api.health.dataquery.service.controller.observation;

import static gov.va.api.lighthouse.vulcan.Rules.ifParameter;
import static gov.va.api.lighthouse.vulcan.Rules.parametersNeverSpecifiedTogether;
import static gov.va.api.lighthouse.vulcan.Vulcan.returnNothing;

import gov.va.api.health.dataquery.service.config.LinkProperties;
import gov.va.api.health.dataquery.service.controller.WitnessProtection;
import gov.va.api.health.dataquery.service.controller.vulcanizer.Bundling;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedBundler;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedReader;
import gov.va.api.health.dataquery.service.controller.vulcanizer.VulcanizedTransformation;
import gov.va.api.health.r4.api.resources.Observation;
import gov.va.api.lighthouse.vulcan.Vulcan;
import gov.va.api.lighthouse.vulcan.VulcanConfiguration;
import gov.va.api.lighthouse.vulcan.mappings.Mappings;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

  private final ObservationRepository repository;

  private final WitnessProtection witnessProtection;

  private final LinkProperties linkProperties;

  private VulcanConfiguration<ObservationEntity> configuration() {
    return VulcanConfiguration.<ObservationEntity>forEntity(ObservationEntity.class)
        .paging(linkProperties.pagingConfiguration("Observation", ObservationEntity.naturalOrder()))
        .mappings(
            Mappings.forEntity(ObservationEntity.class)
                .token("category", this::tokenCategoryIsSupported, this::tokenCategoryValues)
                .token("code", this::tokenCodeIsSupported, this::tokenCodeValues)
                .value("_id", "cdwId", witnessProtection::toCdwId)
                .value("identifier", "cdwId", witnessProtection::toCdwId)
                .dateAsInstant("date", "dateUtc")
                .string("patient", "icn")
                .get())
        .rule(parametersNeverSpecifiedTogether("_id", "identifier"))
        .rule(ifParameter("date").thenAlsoAtLeastOneParameterOf("category"))
        .rule(ifParameter("category").thenAlsoAtLeastOneParameterOf("patient"))
        .rule(ifParameter("code").thenAlsoAtLeastOneParameterOf("patient"))
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

  Set<String> toCdwCategory(String fhirCategory) {
    if (fhirCategory == null) {
      throw new IllegalArgumentException("Category is null");
    }
    return Set.of(fhirCategory);
  }

  Set<String> toCdwCode(String fhirCode) {
    if (fhirCode == null) {
      throw new IllegalArgumentException("Code is null");
    }
    return Set.of(fhirCode);
  }

  boolean tokenCategoryIsSupported(gov.va.api.lighthouse.vulcan.mappings.TokenParameter token) {
    return (token.hasSupportedSystem(OBSERVATION_CATEGORY_SYSTEM) && token.hasExplicitSystem())
        || token.hasAnySystem();
  }

  Collection<String> tokenCategoryValues(
      gov.va.api.lighthouse.vulcan.mappings.TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndExplicitCode((s, c) -> toCdwCategory(c))
        .onAnySystemAndExplicitCode(this::toCdwCategory)
        .onNoSystemAndExplicitCode(this::toCdwCategory)
        .onExplicitSystemAndAnyCode(s -> Set.of())
        .build()
        .execute();
  }

  boolean tokenCodeIsSupported(gov.va.api.lighthouse.vulcan.mappings.TokenParameter token) {
    return (token.hasSupportedSystem(OBSERVATION_CODE_SYSTEM) && token.hasExplicitSystem())
        || token.hasAnySystem();
  }

  Collection<String> tokenCodeValues(gov.va.api.lighthouse.vulcan.mappings.TokenParameter token) {
    return token
        .behavior()
        .onExplicitSystemAndExplicitCode((s, c) -> toCdwCode(c))
        .onAnySystemAndExplicitCode(this::toCdwCode)
        .onNoSystemAndExplicitCode(this::toCdwCode)
        .onExplicitSystemAndAnyCode(s -> Set.of())
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

  VulcanizedReader<ObservationEntity, DatamartObservation, Observation> vulcanizedReader() {
    return VulcanizedReader.forTransformation(transformation())
        .repository(repository)
        .toPatientId(e -> Optional.of(e.icn()))
        .toPayload(ObservationEntity::payload)
        .build();
  }
}
